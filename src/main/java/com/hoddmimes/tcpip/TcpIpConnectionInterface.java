package com.hoddmimes.tcpip;

import java.io.IOException;

/**
 * Interface to tcp/ip transport handle. The transport handle are created 
 * for outgoing client connections {@link TcpIpClient#connect(TcpIpConnectionTypes, String, int, TcpIpConnectionCallbackInterface)} 
 * or for inbound connection accepted {@link TcpIpServerCallbackInterface}
 */
public interface TcpIpConnectionInterface extends TcpIpCountingInterface 
{
	/**
	 * Set and associate a user object with the transport handle i.e. connection
	 * @param pObject user object
	 */
	public void setUserCntx(Object pObject);

	/**
	 * Retrieve user object associated with the transport handle i.e. connection
	 * @return user object 
	 */
	public Object getUserCntx();

	/**
	 * Close connection
	 */
	void close();

	/**
	 * Send data to counterpart.
	 * @param pBuffer byte vector to be sent to counterpart.
	 * @param pFlush flag to indicate if a flush is to be performed after data is written to the socket. 
	 * @throws IOException exception throw in case of failure during transmission of data. 
	 */
	public void write(byte[] pBuffer, boolean pFlush) throws IOException;

	/**
	 * Send data to counterpart.
	 * @param pBuffer byte vector to be sent to counterpart.
	 * @param pOffset starting position within vector  
	 * @param pLength length of data to be sent
	 * @param pFlush flag to indicate if a flush is to be performed after data is written to the socket.
	 * @throws IOException exception throw in case of failure during transmission of data.
	 */
	public void write( byte[] pBuffer, int pOffset, int pLength, boolean pFlush) throws IOException;
	
	/**
	 * Send data to counterpart. Flush of data is implicit.
	 * @param pBuffer byte vector to be sent to counterpart.
	 * @throws IOException exception throw in case of failure during transmission of data. 
	 */
	public void write(byte[] pBuffer) throws IOException;
	
	/**
	 * Send data to counterpart. Flush of data is implicit.
	 * @param pBuffer byte vector to be sent to counterpart.
	 * @param pOffset starting position within vector  
	 * @param pLength length of data to be sent
	 * @throws IOException exception throw in case of failure during transmission of data.
	 */
	
	public void write( byte[] pBuffer, int pOffset, int pLength) throws IOException;

	
	
	
}
