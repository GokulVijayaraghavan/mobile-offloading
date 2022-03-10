package com.example.slavecalc;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Thread Thread1 = null;
    EditText etIP, etPort;
    TextView tvMessages;
    EditText etMessage;
    Button btnSend;
    String SERVER_IP;
    LocationManager locationManager;
    BatteryManager bm;
    int SERVER_PORT;
    private String inps = "";
    String latitude = "";
    String longitude = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
        tvMessages = findViewById(R.id.tvMessages);

//        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        Button btnConnect = findViewById(R.id.btnConnect);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager = (LocationManager)getSystemService(this.LOCATION_SERVICE);

        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        double lat = locationGPS.getLatitude();
        double longi = locationGPS.getLongitude();
        latitude = String.valueOf(lat);
        longitude= String.valueOf(longi);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tvMessages.setText("");
                SERVER_IP = etIP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
                Thread1 = new Thread(new Thread1());
                Thread1.start();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Start";

                if (!message.isEmpty()) {
                    new Thread(new Thread3(message)).start();
                    tvMessages.setText("Calculating\n");
                }
            }
        });
    }
    private DataOutputStream output;
    private DataInputStream input;
//    private LocationManager locationManager;
    class Thread1 implements Runnable {

        @Override
        public void run() {
            Socket socket;
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new DataOutputStream(socket.getOutputStream());
                input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessages.setText("Connected\n");
                    }
                    // send battery info and location info to master
                });

                bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
                int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

                String s = "BATTERY:"+String.valueOf(percentage) +",LAT:"+latitude+",LONG:"+longitude;
                output.writeUTF(s);
                new Thread(new Thread2()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class Thread2 implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readUTF();
                    if (message  != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMessages.setText(message + "\n");
//                                tvMessages.setMovementMethod(new ScrollingMovementMethod());
                                inps = inps + message + "\n";
                            }
                        });
                    } else {
                        tvMessages.setText("Receiving Data" + "." + "\n");
                        Thread1 = new Thread(new Thread1());
                        Thread1.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public int[][] get_2d_mat(String s){ //Convert Single line 2dArray to 2d Int Matrix

        s=s.replace("[","");//replacing all [ to ""
        s=s.substring(0,s.length()-2);//ignoring last two ]]
        String s1[]=s.split("],");//separating all by "],"

        int my_matrics[][] = new int[s1.length][s1.length];//declaring two dimensional matrix for input

        for(int i=0;i<s1.length;i++){
            s1[i]=s1[i].trim();//ignoring all extra space if the string s1[i] has
            String single_int[]=s1[i].split(", ");//separating integers by ", "

            for(int j=0;j<single_int.length;j++){
                my_matrics[i][j]=Integer.parseInt(single_int[j]);//adding single values
            }
        }
        return my_matrics;
    }
    class Thread3 implements Runnable {
        private String message;
        Thread3(String message) {
            this.message = message;
        }
        @Override

        public void run() {

            String[] lines = inps.split(System.getProperty("line.separator"));
            int[][] matrix = get_2d_mat(lines[0]);
            String res = "";
            long totalTime = 0;
            long totalEnergy = 0;
            int mult = 0;
            int i = 1;
            String idx = "";
            String send_meta = "";
            long startTime = System.nanoTime(); // start time
            long start_energy = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);
            for(i = 1; i<= lines.length -1; i++){


                for(int x = 0; x< matrix.length; x++){
                    for(int y = 0; y< matrix[0].length;y++){
                        String strx = lines[i].substring(1,lines[i].length()-1);
                        String single_int[] =strx.split(", ");
                        idx = single_int[single_int.length-1];

                        mult += matrix[x][y] * Integer.parseInt(String.valueOf(single_int[y]));
                    }
                    res = res + String.valueOf(mult) + " ";
                    mult = 0;
                }

                res = res  +idx+ "\n";

                long endTime   = System.nanoTime(); //end timer
                long end_energy = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);
                totalTime = endTime - startTime; //Run time in nano-secs
                totalEnergy = Math.abs(end_energy) - Math.abs(start_energy);
                send_meta = "RUNTIME:"+String.valueOf(totalTime)+",ENERGY:"+String.valueOf(totalEnergy);
            }
            try {
                output.writeUTF(res);

                output.writeUTF(send_meta);
            } catch (IOException e) {
                e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMessages.append("Result Sent" + "." + "\n");
//                    etMessage.setText("");
                }
            });
        }
    }
}