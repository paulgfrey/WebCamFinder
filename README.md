<PRE>
WebCamFinder
============

IPCamera Search Utility that find Wansview/Foscam MJPEG cameras on local network (Intranet).

There is a windows search utility that is supplied with Wansview/Foscam MJPEG IP Cameras but I needed something that would work in both Windows and Linux.

To execute as a standalone utility:
1.  Compile the java files into WebCamFinder.jar
2.  java -cp WebCamFinder.jar com.pf.webcam.WebCamFinder

You can also include it as part of your project:
1.  Include the WebCamFinder.jar in your project class path.
2.  Create a new instance of WebCamFinder().
3.  Use the method findList() to find cameras on your local network.  This method returns a List of WebCamBean objects.
4.  An alternative method to allow easier searching by camera ID is findMap().  This method returns a Map of WebCamBean objects with the Camera ID as the key.
5.  You can use the method sendInitRequest(WebCamBean newWebCamBean, String originalIP, String factoryUserName, String factoryPassword) to set the camera's network settings.
</PRE>
