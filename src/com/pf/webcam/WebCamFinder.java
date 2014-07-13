package com.pf.webcam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class will use the IPCamera Search Protocol over an UDP socket to find cameras on the current network.  It will also 
 * allow us to change network address settings on the camera.
 * 
 * <PRE>
 * Specifications on the Wansview/Foscam MJPEG IP Camera Search Protocol:
 * 
 * Introduction
 * With this protocol, you can search and change the network
 * configuration of ipcameras with intranet.
 * 
 * Procedure: PC client broadcasts search packages over intranet via
 * port 10000. Whenever the ipcamera receives the packages, it
 * broadcasts the response packages in return via the original port
 * which it gets the search packages.
 * 
 * Configuration: PC client broadcasts search packages over intranet
 * via port 10000. Whenever the ipcamera receives the packages, it
 * checks whether it’s the proper target, when the check result is true,
 * it makes configuration accordingly and returns the operation result
 * to the original port which it gets the search packages.
 * 
 * The above operations are based on UDP protocol.
 * 
 * Data Format Legend:
 * 	Data Type length（unit: Byte） Sort
 * 	INT8 1
 * 	INT16 2 low byte leads, high byte follows
 * 	INT16_R 2 low byte follows, high byte leads
 * 	INT32 4 low byte leads, high byte follows
 * 	INT32_R 4 low byte follows, high byte leads
 * 	BINARY_STREAM N
 * 
 * Command Format
 * All protocols are combined by multiple commands. Every command has its own format:
 * 	Combination Type Description
 * 	Header BINARY_STREAM[4] Camera operation protocol："MO_I"
 * 	Operation Code INT16 use to differentiate commands within a protocol
 * 	Reserved INT8 =0
 * 	Reserved BINARY_STREAM[8]
 * 	Text Length INT32 text length within the commands
 * 	Reserved INT32
 * 	Text BINARY_STREAM[n] text of the command
 * 
 * Search_Req（Search Command）
 * 1) Administration user broadcasts this command to the network, with which can collect the basic 
 * information of all cameras connected to the intranet.
 * 2) Operation Code：0
 * 3) Command forth ： administration user -> broadcast address （255.255.255.255, port 10000）
 * 4) text field：
 * 	Field Type Description
 * 	Reserved INT8 =0
 * 	Reserved INT8 =0
 * 	Reserved INT8 =0
 * 	Reserved INT8 =1
 * 
 * Search_Resp（Search Responds Command）
 * 1) Whenever a camera receives the command Search_Req, it responses with its network configuration 
 * and product information and broadcast to the network.
 * 2) Operation Code：1
 * 3) Command forth ： camera -> broadcast address （ 255.255.255.255 ，same port it receives the Search_Req command）
 * 4) Text field：
 * 	Field Type Description
 * 	Camera ID BINARY_STREAM[13]
 * 	Camera Name BINARY_STREAM[21]
 * 	IP INT32_R
 * 	Subnet mask INT32_R
 * 	Gateway IP INT32_R
 * 	DNS INT32_R
 * 	Reserved BINARY_STREAM[4]
 * 	Sys_software version BINARY_STREAM[4] a.b.c.d
 * 	App_software version BINARY_STREAM[4] a.b.c.d
 * 	Camera port INT16_R
 * 	dhcp enabled INT8 0 ： dhcp disabled ； 1 ： dhcp enabled
 * 			Note：this field is only valid for firmware later than x.x.2.2
 * 
 * Init_Req（Network Setting Initiation Command）
 * 1) With broadcasting this command, user can initiate the network settings for cameras.
 * 2) Operation Code：2
 * 3) Command forth ： administration user -> broadcast address （255.255.255.255，port 10000）
 * 4) text field：
 * 	Field Type Description
 * 	Reserved INT8 =0
 * 	Reserved INT8 =0
 * 	Reserved INT8 =0
 * 	Reserved INT8 =1
 * 	Camera ID BINARY_STREAM[13]
 * 	User BINARY_STREAM[13]
 * 	Password BINARY_STREAM[13]
 * 	IP INT32_R
 * 	Subnet mask INT32_R
 * 	Gateway IP INT32_R
 * 	DNS INT32_R
 * 	Camera Port INT16_R
 * 
 * Init_Resp（Response Command for Network Setting Initiation）
 * 1) Whenever a camera receives the command Init_Req，it verifies the ID within the command. 
 * If the verification returns true, the camera make configuration accordingly and responses 
 * to the network with this command.
 * 2) Operation Code：3
 * 3) Command forth ： camera -> broadcast address （ 255.255.255.255 ，same port it receives the Init_Req command）
 * 4) Text Field：
 * 	Field Type Description
 * 	Setting Result INT16 0：success, 1：user error, 5 pwd err, 6 pri err
 * </PRE>
 * 
 * @author paulf
 *
 */
public class WebCamFinder {
	private Logger logger = Logger.getLogger("");
	public final static short SUCCESS = 0;
	public final static short USER_ERROR = 1;
	public final static short PASSWORD_ERROR = 5;
	public final static short PRI_ERROR = 6;
	public final static short UNKNOWN_ERROR = -1;

	public WebCamFinder() {

	}
	
	/**
	 * Return a searchable map of the cameras ID's.  Key is the camera ID.
	 * 
	 * @return
	 * @throws IOException
	 */
	public Map<String, WebCamBean> findMap() throws IOException {
		Map<String, WebCamBean> rtnMap = new HashMap<String, WebCamBean>();
		
		List<WebCamBean> list = findList();
		for(WebCamBean webCamBean : list) {
			rtnMap.put(webCamBean.getCameraID(), webCamBean);
		}
		
		return(rtnMap);
	}
	
	/**
	 * This sends a initialization request to the camera changing the camera's network settings.
	 * 
	 * @param newWebCamBean - contains the new settings
	 * @param originalIP - the orginal IP address of the camera
	 * @param factoryUserName
	 * @param factoryPassword
	 * @return SUCCESS, USER_ERROR, PASSWORD_ERROR or PRI_ERROR
	 * @throws IOException
	 */
	public int sendInitRequest(WebCamBean newWebCamBean, String originalIP, String factoryUserName, String factoryPassword) throws IOException {
		int rtnValue = UNKNOWN_ERROR;
		/*
		 * 0000   4d 4f 5f 49 02 00 00 00 00 00 00 00 00 00 00 40  MO_I...........@
		 * 0010   00 00 00 00 00 00 00 00 00 00 01 37 38 41 35 44  ...........78A5D
		 * 0020   44 30 30 43 37 37 44 00 61 64 6d 69 6e 00 00 00  D00C77D.admin...
		 * 0030   00 00 00 00 00 31 32 33 34 35 36 00 00 00 00 00  .....123456.....
		 * 0040   00 00 c0 a8 01 a6 ff ff ff 00 c0 a8 01 01 4b 4b  ..............KK
		 * 0050   4b 4b 00 50 00 00 00                             KK.P...
		 */
		ByteArrayOutputStream outp = new ByteArrayOutputStream();
		outp.write("MO_I".getBytes());
		// Operation Code INT16
		outp.write(new byte[]{0x02, 0x00});
		// Reserve INT8
		outp.write(new byte[]{0x00});
		// Reserve BINARY_STREAM[8]
		for(int i = 0; i < 8; i++) {
			outp.write(new byte[]{0x00});
		}
		// Text length
		outp.write(new byte[]{0x40, 0x00, 0x00, 0x00});
		// UNKNOWN
		outp.write(new byte[]{0x00, 0x00, 0x00, 0x00});
		// Reserved
		outp.write(new byte[]{0x00, 0x00, 0x00, 0x01});
		
		// Text
		outp.write(WebCamFinderUtils.getFixedField(newWebCamBean.getCameraID(), 13));
		outp.write(WebCamFinderUtils.getFixedField(factoryUserName, 13));
		outp.write(WebCamFinderUtils.getFixedField(factoryPassword, 13));
		outp.write(WebCamFinderUtils.getIPAddressAsBytes(newWebCamBean.getIpAddress()));
		outp.write(WebCamFinderUtils.getIPAddressAsBytes(newWebCamBean.getSubnetMask()));
		outp.write(WebCamFinderUtils.getIPAddressAsBytes(newWebCamBean.getGatewayIP()));
		outp.write(WebCamFinderUtils.getIPAddressAsBytes(newWebCamBean.getDNS()));
		int port = Integer.valueOf(newWebCamBean.getCameraPort()).intValue();
		outp.write(WebCamFinderUtils.convIntToINT16(port));
		// pad out to text message to 64
		outp.write(new byte[]{0x00, 0x00, 0x00});
		
		String hexArray = WebCamFinderUtils.byteArrayToHex(outp.toByteArray());
		logger.info("Send Request: " + hexArray);
		
		DatagramSocket socket = null;
		try {
			DatagramPacket sendPacket = new DatagramPacket(outp.toByteArray(), outp.size());

			// Create an address
			InetAddress destAddress = InetAddress.getByName("255.255.255.255");
			sendPacket.setAddress(destAddress);
			sendPacket.setPort(10000);

			socket = new DatagramSocket();
			socket.setBroadcast(true);
			socket.setReuseAddress(true);
			socket.setSoTimeout(5000);
			socket.send(sendPacket);
			logger.fine("Sent: " + WebCamFinderUtils.byteArrayToHex(sendPacket.getData()));

			byte[] b = new byte[1024];
			DatagramPacket dgram = new DatagramPacket(b, b.length);
			
			while(true) {
				try {
					socket.receive(dgram); // blocks until a datagram is received
				} catch (SocketTimeoutException se) {
					break;
				}
				logger.fine("Received: " + WebCamFinderUtils.byteArrayToHex(dgram.getData()));
				InetAddress sourceIP = dgram.getAddress();
				if(sourceIP.getHostAddress().contains(originalIP)) {
					// Get the last 2 bytes
					byte[] rtnValueBytes = Arrays.copyOfRange(dgram.getData(), (dgram.getLength()-2), dgram.getLength());
					rtnValue = WebCamFinderUtils.convINT16ToShort(rtnValueBytes);
					logger.fine("rtnValue=" + rtnValue);
					break;
				}
				dgram.setLength(b.length); // must reset length field!
			}
		}
		catch(Exception e) {
			throw(new IOException("Unable to send init_req packet.", e));
		}
		finally {
			if(socket != null) {
				socket.close();
			}
		}

		return(rtnValue);
	}
	
	/**
	 * This method sends out the search request to all the IP Cameras on the network.
	 * 
	 * @return list of IP Camera's found.
	 * @throws IOException
	 */
	public List<WebCamBean> findList() throws IOException {
		List<WebCamBean> foundList = new ArrayList<WebCamBean>();

		DatagramSocket socket = null;
		try {
			byte[] sendData = new byte[]{0x4d, 0x4f, 0x5f, 0x49, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04,
										0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
			/*
			 * 0000   4d 4f 5f 49 00 00 00 00 00 00 00 00 00 00 00 04
			 * 0010   00 00 00 00 00 00 00 00 00 00 01
			 */
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);

			// Create an address
			InetAddress destAddress = InetAddress.getByName("255.255.255.255");
			sendPacket.setAddress(destAddress);
			sendPacket.setPort(10000);

			socket = new DatagramSocket();
			socket.setBroadcast(true);
			socket.setReuseAddress(true);
			socket.setSoTimeout(5000);
			socket.send(sendPacket);

			byte[] b = new byte[1024];
			DatagramPacket dgram = new DatagramPacket(b, b.length);
			
			while(true) {
				try {
					socket.receive(dgram); // blocks until a datagram is received
				} catch (SocketTimeoutException se) {
					break;
				}
				logger.fine(WebCamFinderUtils.byteArrayToHex(dgram.getData()));
				foundList.add(getBean(dgram.getData()));
				dgram.setLength(b.length); // must reset length field!
			}
		}
		catch(Exception e) {
			throw(new IOException("Unable to search.", e));
		}
		finally {
			if(socket != null) {
				socket.close();
			}
		}
		
		return(foundList);	
	}
	
	/**
	 * Converts the response from the IPCamera into our WebCamBean.
	 * 
	 * @param respData
	 * @return
	 */
	private WebCamBean getBean(byte[] respData) {
		WebCamBean rtnBean = new WebCamBean();
		
		rtnBean.setCameraID(WebCamFinderUtils.getTrimmed(Arrays.copyOfRange(respData,23,35)));
		rtnBean.setCameraName(WebCamFinderUtils.getTrimmed(Arrays.copyOfRange(respData,36,56)));
		rtnBean.setIpAddress(Arrays.copyOfRange(respData,57,61));
		rtnBean.setSubnetMask(Arrays.copyOfRange(respData,61,65));
		rtnBean.setGatewayIP(Arrays.copyOfRange(respData,65,69));
		rtnBean.setDNS(Arrays.copyOfRange(respData,69,73));
		// 4 bytes reserved
		rtnBean.setSysSoftwareVers(Arrays.copyOfRange(respData,77,81));
		rtnBean.setAppSoftwareVers(Arrays.copyOfRange(respData,81,85));
		rtnBean.setCameraPort(Arrays.copyOfRange(respData,85,87));
	
		return rtnBean;		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WebCamFinder webCamFinder = new WebCamFinder();
		try {
			List<WebCamBean> foundList = webCamFinder.findList();
			for(WebCamBean webCamBean: foundList) {
				System.out.println(webCamBean);
			}
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
		}
	}

}
