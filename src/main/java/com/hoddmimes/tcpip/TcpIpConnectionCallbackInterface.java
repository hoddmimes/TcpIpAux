package com.hoddmimes.tcpip;

import java.io.IOException;

/**
 * Interface for handling transport asynchronous events. Application will be notified when a message 
 * is received from its counterpart and if a fatal connection error occurs.
 */
public interface TcpIpConnectionCallbackInterface {
	
	/**
	 * Method to be invoked when a message is received 
	 * @param pConnection the connection on which the message was received
	 * @param pBuffer buffer with data received
	 */
	public void tcpipMessageRead(TcpIpConnectionInterface pConnection, byte[] pBuffer);

	/**
	 * Method invoked when a fatal error has occurred on transport.
	 * @param pConnection the connection on which the exception has occured 
	 * @param e error exception
	 */
	public void tcpipClientError(TcpIpConnectionInterface pConnection, Exception e);

}
