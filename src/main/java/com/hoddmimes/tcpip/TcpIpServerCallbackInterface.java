package com.hoddmimes.tcpip;

/**
 * Callback interface for handling server events. Besides the connection events when 
 * messages are received and fatal exception occurs events will also be signaled when 
 * an inbound connection is established.
 *
 */
public interface TcpIpServerCallbackInterface extends TcpIpConnectionCallbackInterface 
{
	/**
	 * Method to be invoked when a client connects to the server
	 * @param pConnection connection context for the client being connected.
	 */
	public void tcpipInboundConnection(TcpIpConnectionInterface pConnection);
}
