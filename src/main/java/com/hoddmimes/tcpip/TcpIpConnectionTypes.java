package com.hoddmimes.tcpip;

/**
 * Enumeration types defining types of connections that can be established
 * <ul>
 * <li><b>Plain</b> ordinary connection, no compression or encryption
 * <li><b>Encrypt</b> encrypted session using AES 256 bit in CBC mode
 * <li><b>Compression_Encrypt</b> compress and encrypted session
 * <li><b>Compression</b> compressed session using ZLIB
 * </ul>
 */

public enum TcpIpConnectionTypes 
{
	Plain(1), 				    // Vanilla tcp/ip socket no extravaganza
	Encrypt(2), 				// AES 256 bit encrypted socket
	Compression_Encrypt(3), 	// ZLIB compression over AES 256 bit encrypted socket
	Compression(4);	 	        // ZLIB compression over a vanilla socket

	private final int mValue;

	private TcpIpConnectionTypes(int value) {
		this.mValue = value;
	}

	public static TcpIpConnectionTypes decode( int pValue ) {
		switch( pValue ) {
			case 1 : return TcpIpConnectionTypes.Plain;
			case 2 : return TcpIpConnectionTypes.Encrypt;
			case 3: return TcpIpConnectionTypes.Compression_Encrypt;
			case 4 : return TcpIpConnectionTypes.Compression;
		}
		return null;
	}

	public int encode() {
		return this.mValue;
	}

}
