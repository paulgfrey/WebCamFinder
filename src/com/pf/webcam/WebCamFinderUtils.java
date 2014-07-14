package com.pf.webcam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class WebCamFinderUtils {
	
	/**
	 * Returns a byte array that contains the value padded to size with zero.
	 * 
	 * @param value string to copy
	 * @param size of field
	 * @return padded byte array
	 */
	public final static byte[] getFixedField(String value, int size) {
		ByteBuffer buf = ByteBuffer.allocate(size);
		buf.put(value.getBytes());
		
		return(buf.array());
	}
	
	/**
	 * Converts an integer to an INT16.
	 * 
	 * @param intVal
	 * @return
	 */
	public final static byte[] convIntToINT16(int intVal) {
		ByteBuffer buf = ByteBuffer.allocate(2);
		buf.order(ByteOrder.BIG_ENDIAN);
		buf.putShort((short)intVal);
		
		return(buf.array());
	}
	
	/**
	 * Converts an INT16 to a short.
	 * 
	 * @param bytes
	 * @return
	 */
	public final static short convINT16ToShort(byte[] bytes) {
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		
		return(buf.getShort());		
	}
	
	public final static byte[] getIPAddressAsBytes(String ipAddress) throws IOException {
		byte[] rtnBytes = null;
		
		ByteArrayOutputStream outp = new ByteArrayOutputStream();
		
		InetAddress ip = InetAddress.getByName(ipAddress);
		byte[] tmpByteArr = new byte[1];
		for(byte b : ip.getAddress()) {
			tmpByteArr[0] = b;
			outp.write(tmpByteArr);
		}
		rtnBytes = outp.toByteArray();
		outp.close();
		
		return(rtnBytes);
	}
	
	public final static String byteArrayToHex(byte[] a) {
		   StringBuilder sb = new StringBuilder(a.length * 2);
		   for(byte b: a)
		      sb.append(String.format("%02x ", b & 0xff));
		   return sb.toString();
	}
	
	public final static String getTrimmed(byte[] data) {
		boolean zeroFound = false;
		for(int i = 0; i < data.length; i++) {
			if(data[i] == 0) {
				zeroFound = true;
			}
			if(zeroFound) {
				data[i] = 0;
			}
		}
		
		return((new String(data)).trim());
	}
}
