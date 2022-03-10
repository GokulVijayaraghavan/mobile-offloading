To Run the app

1. Install Master_calc.apk on master mobile, Slave_calc.apk on slave mobile(s).
2. Both of the application need to be given permissions manually
	a. After installation, locate the app in your app menu
	b. Long press the application > app info
	c. Go to permissions tab and allow all permissions that were denied
		i.Permission required are Location, Files and storage


To perform matrix multiplication

1. Open Master app and note the IP address and Port of the master
2. On a seperate phone, Open the slave app(s) and enter the noted IP and port of the master click connect
3. Wait for the connection message and the initial information exchange (Battery, Latitude, and longitude)
4. In the master mobile, click "SEND" to distribute the matrix
5. On the slave app(s), you will see the last column that was distributed, use this to ensure there were no information lost
6. On each slave app(s), click send to start the calucation
7. Wait for the "result sent" message on each app(s)
8. Muliplied matrix is shown in the scroll window
