package com.hoddmimes.tcpip;

import java.io.IOException;

/**
 * Enumeration types defining types of connections that can be established
 * <ul>
 * <li><b>Plain</b> ordinary connection, no compression or encryption
 * <li><b>Encrypt</b> encrypted session using AES 256 bit in CBC mode
 * <li><b>Compression_Encrypt</b> compress and encrypted session
 * <li><b>Compression</b> compressed session using ZLIB
 * </ul>
 */

public class TcpIpConnectionTypes
{
	public static int PLAIN = 1;
	public static int ENCRYPT = 2;
	public static int COMPRESS = 4;
	public static int SSH_SIGNING = 8;


	public static void validate(int pOptions) throws IOException{
		if ((pOptions & (PLAIN+ENCRYPT)) != 0) {
			throw new IOException("Connection must be PLAIN or ENCRYPTED can not be both");
		}
	}

	public static String toString( int pOptions ) {
		StringBuilder sb = new StringBuilder();
		if ((pOptions & PLAIN) != 0) {
			sb.append("PLAIN,");
		}
		if ((pOptions & ENCRYPT) != 0) {
			sb.append("ENCRYPT,");
		}
		if ((pOptions & COMPRESS) != 0) {
			sb.append("COMPRESS,");
		}
		if ((pOptions & SSH_SIGNING) != 0) {
			sb.append("SSH_SIGNING,");
		}

		if (sb.toString().length() > 1) {
			return sb.toString().substring(0, sb.toString().length() - 1);
		}
		return "";
	}
}
