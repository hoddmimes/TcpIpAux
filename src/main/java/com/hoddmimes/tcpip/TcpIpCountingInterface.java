package com.hoddmimes.tcpip;

/**
 * Interface for retrieving compression statistics.
 * Applicable when using compressed sessions.
 */
public interface TcpIpCountingInterface 
{	
	/**
	 * Retrieves number bytes written 
	 * @return bytes written 
	 */
	public long getBytesWritten();

	/**
	 * Retrieves number of bytes read 
	 * @return bytes read
	 */
	public long getBytesRead();

	/**
	 * Get compression rate for sent data. (Uncompressed - Compressed / Uncompressed)
	 * Multiply with 100 to get percentage.
	 * @return compression rate 
	 */
	public double getOutputCompressionRate();

	/**
	 * Get compression rate for received data. (Uncompressed - Compressed / Uncompressed)
	 * Multiply with 100 to get percentage. 
	 * @return compression rate 
	 */
	public double getInputCompressionRate();

}
