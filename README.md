WebCamFinder
============
<PRE>
IPCamera Search Utility that find Wansview/Foscam MJPEG cameras on local network (Intranet).

There is a windows search utility that is supplied with Wansview/Foscam MJPEG IP Cameras but I needed something that would work in both Windows and Linux.

To execute as a standalone search utility:
1.  Compile the java files into WebCamFinder.jar
2.  java -cp WebCamFinder.jar com.pf.webcam.WebCamFinder

You can also include it as part of your project:
1.  Include the WebCamFinder.jar in your project class path.
2.  Create a new instance of WebCamFinder().
3.  Use the method findList() to find cameras on your local network.  This method returns a List of WebCamBean objects.
4.  An alternative method to allow easier searching by camera ID is findMap().  This method returns a Map of WebCamBean objects with the Camera ID as the key.
5.  You can use the method sendInitRequest(WebCamBean newWebCamBean, String originalIP, String factoryUserName, String factoryPassword) to set the camera's network settings.

To execute as a standalone initializing utility (set the IP Address and Port for the camera):
1.  First you will need to execute a search to find the Camera ID for the camera you want to initialize (see above).
2.  Then you can run the following command, entering the parameters that meet your system:
java -cp WebCamFinder.jar com.pf.webcam.WebCamFinder [-cameraID=<camera ID> -newIP=<IP Address> [-newPort=<port number> -factoryUserName=<user name> -factoryPassword=<password> -newGateway=<IP Address> -newDNS=<IP Address> -newNetMask=<Net Mask>]]

Defaults:
	-factoryUserName: admin
	-factoryPassword: 123456
	-newPort: 80
	-newGateway: 192.168.1.1
	-newDNS: 192.168.1.1
	-newNetMask: 255.255.255.0
	
An example:
java -cp WebCamFinder.jar com.pf.webcam.WebCamFinder -cameraID=78A5DD0XXXXX -newIP=192.168.1.50 -newPort=50001

Remember you have to actually reboot the camera for the settings to take.
</PRE>
