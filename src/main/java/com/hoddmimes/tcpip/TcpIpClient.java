package com.hoddmimes.tcpip;

import com.hoddmimes.tcpip.crypto.CBCBlockCipher;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyStore;
import java.util.zip.ZipInputStream;
//import java.security.cert.Certificate;
//import java.util.Enumeration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;



/**
 * The TcpIpClient allows programs to create a connection to a server. After successfully established the connection 
 * the program can receive and send messages to the server.
 *
 */
public class TcpIpClient
{
	/**
	 * Simple constructor for creating a connection to a server.  
	 * @param pHost Ip host address, either a IP host number (xxx.xxx.xxx.xxx) or a host name (foo.bar.com)
	 * @param pPort the tcp/ip port on which the server will accept inbound connections on 
	 * @param pCallback interface to be called when messages from the server has been read or to signal communication error
	 * @return handle to the connection
	 * @throws IOException, error exception when failing to create and establish connection 
	 */
	public static TcpIpConnectionInterface connect(String pHost, int pPort, TcpIpConnectionCallbackInterface pCallback) throws Exception {
		return TcpIpClient.connect(TcpIpConnectionTypes.Plain, pHost, pPort, pCallback);
	}

	/**
	 * Constructor for creating a connection to a server.
	 * @param pConnectionType type of connection. <a href="TcpIpConnectionTypes.html">TcpIpConnectionTypes</a> 
	 * @param pHost Ip host address, either a IP host number (xxx.xxx.xxx.xxx) or a host name (foo.bar.com)
	 * @param pPort the tcp/ip port on which the server will accept inbound connections on 
	 * @param pCallback interface to be called when messages from the server has been read or to signal communication error
	 * @return handle to the connection
	 * @throws IOException, error exception when failing to create and establish connection 
	 */
	public static TcpIpConnectionInterface connect(TcpIpConnectionTypes pConnectionType,
			String pHost, int pPort, TcpIpConnectionCallbackInterface pCallback) throws Exception
	{

			SocketAddress tEndpointAddress = new InetSocketAddress(pHost, pPort);
			Socket tSocket = new Socket();
			tSocket.connect(tEndpointAddress, 10000); // 10 seconds timeout

		TcpIpConnection tTcpIpConnection =  new TcpIpConnection(
				tSocket,
				TcpIpConnection.ConnectionDirection.Out, 
				pConnectionType, 
				pCallback);

			tTcpIpConnection.init();
			tTcpIpConnection.start();

        return tTcpIpConnection;
	}
}
