package com.example.mastercalc;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.io.BufferedReader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    ServerSocket serverSocket;
    Thread Thread1 = null;
    TextView tvIP, tvPort;
    TextView tvMessages, resx;
    EditText etMessage;
    Button btnSend;
    LocationManager locationManager;
    double lat, longi;
    public static String SERVER_IP = "";
    public static final int SERVER_PORT = 8080;
    private ArrayList<Thread> serverThreads = new ArrayList<Thread>();
    private ArrayList<DataOutputStream> ops = new ArrayList<DataOutputStream>();
    private int rows = 50;
    private int columns = 50;
    private int [][] myArray = new int[rows][columns];
    private int [][] multi = new int[rows][columns];
    private int [][] result = new int[rows][columns];
    private int[] a = new int[rows +1];
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    String message;
    private String inps = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvIP = findViewById(R.id.tvIP); // Has serverIP
        tvPort = findViewById(R.id.tvPort); // Has Server port
        tvMessages = findViewById(R.id.tvMessages);
        resx = (TextView) findViewById(R.id.res_view);
        resx.setMovementMethod(new ScrollingMovementMethod());
//        resMessages = findViewById(R.id.messageWindow);
//        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        try {
            SERVER_IP = getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

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

        lat = locationGPS.getLatitude();
        longi = locationGPS.getLongitude();

        Thread1 = new Thread(new Thread1());



        Thread1.start();
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                message = etMessage.getText().toString().trim();
                InputStream inputStream = getResources().openRawResource(R.raw.sample); //read from Res
                Scanner sc = new Scanner(new BufferedReader(new InputStreamReader(inputStream)));

                InputStream inputStream2 = getResources().openRawResource(R.raw.multiplier); //read from Res
                Scanner sc2 = new Scanner(new BufferedReader(new InputStreamReader(inputStream2)));

                while(sc.hasNextLine()) {// Read from File1
                    for (int i=0; i<myArray.length; i++) {
                        String[] line = sc.nextLine().trim().split(" ");
                        for (int j=0; j<line.length; j++) {
                            myArray[i][j] = Integer.parseInt(line[j]);
                        }
                    }
                }

                while(sc2.hasNextLine()) {
                    for (int i=0; i<multi.length; i++) {// Read from File2
                        String[] line = sc2.nextLine().trim().split(" ");
                        for (int j=0; j<line.length; j++) {
                            multi[i][j] = Integer.parseInt(line[j]);
                        }
                    }
                }



                new Thread(new Thread3(myArray, multi)).start();

//                if (!message.isEmpty()) {
//                    new Thread(new Thread3(message)).start();
//                }
            }
        });
    }
    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }
    private DataOutputStream output;
    private DataInputStream input;
    class Thread1 implements Runnable {
        @Override
        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessages.setText("Not connected");
                        tvIP.setText("IP: " + SERVER_IP);
                        tvPort.setText("Port: " + String.valueOf(SERVER_PORT));
                    }
                });
                try {
                        while (true) {
                            socket = serverSocket.accept();
                            output = new DataOutputStream(socket.getOutputStream());
                            input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                            String message = input.readUTF();
                            Map<String, Float> myMap = new HashMap<>();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvMessages.setText("Connected\n");
                                    if (message.contains("BATTERY")){

                                        String[] pairs = message.split(",");
                                        for (int i=0;i<pairs.length;i++) {
                                            String pair = pairs[i];
                                            String[] keyValue = pair.split(":");
                                            myMap.put(keyValue[0], Float.valueOf(keyValue[1]));
                                        }
                                        tvMessages.append("BATT:" + myMap.get("BATTERY") + "\n");
                                        tvMessages.append("LAT:" + myMap.get("LAT") + "\n");
                                        tvMessages.append("LONG:" + myMap.get("LONG") + "\n");
                                    }
                                }
                            });
                            sleep(1000);
                            double dist =  Math.sqrt((myMap.get("LAT") - lat) * (myMap.get("LAT") - lat) + (myMap.get("LONG") - longi) * (myMap.get("LONG") - longi));
                            if(myMap.get("BATTERY") >=25 && dist < 150 ){
                                Thread t2 = new Thread(new Thread2(socket, output, input));
                                serverThreads.add(t2);
                                ops.add(output); // Add output socket to ArrayList <Try tuple>
                                t2.start();
                            }
                            else{
                                socket.close();
                            }


                        }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Thread2 implements Runnable {
        private DataOutputStream output;
        private DataInputStream input;
        private Socket socket;
        Thread2 (Socket socket, DataOutputStream output,DataInputStream input ){
            this.socket = socket;
            this.output = output;
            this.input = input;

        }
        public DataOutputStream getOp(){
            return this.output;
        }
        @Override
        public void run() {
            while (true) {
                try {

                     String message = input.readUTF(); //Readobject
                    // if batt,and loc do calculation
                    //else wait
//                    Toast.makeText(MainActivity.this, message,
//                            Toast.LENGTH_SHORT).show(); 

                    if (message != null) {

                        String finalMessage = message;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (finalMessage.contains("RUNTIME")){
                                    Map<String, Float> myMap = new HashMap<String, Float>();
                                    String[] pairs = finalMessage.split(",");
                                    for (int i=0;i<pairs.length;i++) {
                                        String pair = pairs[i];
                                        String[] keyValue = pair.split(":");
                                        myMap.put(keyValue[0], Float.valueOf(keyValue[1]));
                                    }
                                    try {
                                        sleep(1500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    tvMessages.append("client:" + myMap.get("RUNTIME")/ 1000000.0 + "\n");
                                    tvMessages.append("client:" + myMap.get("ENERGY") + "\n");
                                }else{


                                String[] lines = finalMessage.split(System.getProperty("line.separator"));
                                for(int i = 0; i< lines.length ; i++){
                                    String single_int[] =lines[i].split(" ");
                                    int idx = Integer.parseInt(single_int[single_int.length-1]);
                                    for(int x = 0;x< rows;x++){
                                        result[x][idx] = Integer.parseInt(single_int[x]);
                                    }
                                }
                                resx.append("client:" + Arrays.deepToString(result) + "\n");
                            }}
                        });
                    } else {
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
    class Thread3 implements Runnable {
        private String message;
        private DataOutputStream output;
        private int [][] sendArray;
        private int [][] multiplier;

        Thread3(int [][] sendArray, int [][] multiplier) {
            this.sendArray = sendArray;
            this.multiplier = multiplier;
//            this.output = output;
        }
        @Override
        public void run() {
            try {
                for(DataOutputStream i : ops) {
//                    this.output = i;
                    i.writeUTF(Arrays.deepToString(sendArray)); // Sends 1st Array to Slave
                }
                int j,i;
                for( i = 0; i< columns; i++){
                    for(j =0; j< rows; j++){
                        a[j] = multiplier[j][i];
                    }
                    a[j] = i; // Piggy-Back Column Index (use to determine column number)
                    ops.get(i % ops.size()).writeUTF(Arrays.toString(a)); // RoundRobin Through slave List
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

//            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMessages.append("Sending Data" + "." + "\n");
//                    etMessage.setText("");
                }
            });
        }
    }
}