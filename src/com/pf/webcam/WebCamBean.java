package com.pf.webcam;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/*
 * Simple Java Bean to hold the response data from the WebCamFinder utility.
 * 
 */
public class WebCamBean implements Cloneable {
	private String cameraID = null;
	private String cameraName = null;
	private String ipAddress = null;
	private String subnetMask = null;
	private String gatewayIP = null;
	private String DNS = null;
	private String sysSoftwareVers = null;
	private String appSoftwareVers = null;
	private String cameraPort = null;
	
	public WebCamBean() {
	}

	public String getCameraID() {
		return cameraID;
	}

	public void setCameraID(String cameraID) {
		this.cameraID = cameraID;
	}

	public String getCameraName() {
		return cameraName;
	}

	public void setCameraName(String cameraName) {
		this.cameraName = cameraName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public void setIpAddress(byte[] bytes) {
		setIpAddress(getStringFrom4ByteAddress(bytes));
	}
	
	private String getStringFrom4ByteAddress(byte[] bytes) {
		StringBuffer stBuf = new StringBuffer();
		for(int i = 0; i < 4; i++) {
			int b = bytes[i] & 0xff;
			stBuf.append(b);
			if(i < 3) {
				stBuf.append(".");
			}
		}
		return(stBuf.toString());
	}
	
	private String getStringFrom2ByteAddress(byte[] bytes) {
		StringBuffer stBuf = new StringBuffer();
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		buf.order(ByteOrder.BIG_ENDIAN);
		stBuf.append("" + (buf.getShort() & 0xffff));
		
		return(stBuf.toString());
	}

	public String getSubnetMask() {
		return subnetMask;
	}

	public void setSubnetMask(String subnetMask) {
		this.subnetMask = subnetMask;
	}
	
	public void setSubnetMask(byte[] bytes) {
		setSubnetMask(getStringFrom4ByteAddress(bytes));
	}

	public String getGatewayIP() {
		return gatewayIP;
	}

	public void setGatewayIP(String gatewayIP) {
		this.gatewayIP = gatewayIP;
	}
	
	public void setGatewayIP(byte[] bytes) {
		setGatewayIP(getStringFrom4ByteAddress(bytes));
	}

	public String getDNS() {
		if("0.0.0.0".equals(DNS)) {
			return(getGatewayIP());
		}
		return DNS;
	}

	public void setDNS(String dNS) {
		DNS = dNS;
	}
	
	public void setDNS(byte[] bytes) {
		setDNS(getStringFrom4ByteAddress(bytes));
	}

	public String getSysSoftwareVers() {
		return sysSoftwareVers;
	}

	public void setSysSoftwareVers(String sysSoftwareVers) {
		this.sysSoftwareVers = sysSoftwareVers;
	}
	
	public void setSysSoftwareVers(byte[] bytes) {
		setSysSoftwareVers(getStringFrom4ByteAddress(bytes));
	}

	public String getAppSoftwareVers() {
		return appSoftwareVers;
	}

	public void setAppSoftwareVers(String appSoftwareVers) {
		this.appSoftwareVers = appSoftwareVers;
	}
	
	public void setAppSoftwareVers(byte[] bytes) {
		setAppSoftwareVers(getStringFrom4ByteAddress(bytes));
	}

	public String getCameraPort() {
		return cameraPort;
	}

	public void setCameraPort(String cameraPort) {
		this.cameraPort = cameraPort;
	}
	
	public void setCameraPort(byte[] bytes) {
		setCameraPort(getStringFrom2ByteAddress(bytes));
	}

	public String getAdminURL() {
		return("http://" + getIpAddress() + ":" + getCameraPort());
	}

	@Override
	public String toString() {
		StringBuffer stbuf = new StringBuffer();
		stbuf.append("adminURL=" + getAdminURL() + ", ");
		stbuf.append("cameraID=" + getCameraID() + ", ");
		stbuf.append("cameraName=" + getCameraName() + ", ");
		stbuf.append("ipAddress=" + getIpAddress() + ", ");
		stbuf.append("subnetMask=" + getSubnetMask() + ", ");
		stbuf.append("gatewayIP=" + getGatewayIP() + ", ");
		stbuf.append("DNS=" + getDNS() + ", ");
		stbuf.append("sysSoftwareVers=" + getSysSoftwareVers() + ", ");
		stbuf.append("appSoftwareVers=" + getAppSoftwareVers() + ", ");
		stbuf.append("cameraPort=" + getCameraPort() + ", ");
		
		return(stbuf.toString());
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
