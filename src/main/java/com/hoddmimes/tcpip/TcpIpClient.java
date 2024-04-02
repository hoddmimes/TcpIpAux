package com.hoddmimes.tcpip;

import com.hoddmimes.tcpip.impl.Signer;
import com.hoddmimes.tcpip.messages.IncompatibleConnectionException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import java.security.Security;
//import java.security.cert.Certificate;
//import java.util.Enumeration;


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
	 * @throws IOException error exception when failing to create and establish connection
	 * @throws IncompatibleConnectionException  thrown when connecting to a server being started to accept s specific
	 * features i.e. plain, compressed, encrypted or eompressed_encrypted connections.
	 */
	public static TcpIpConnectionInterface connect(String pHost, int pPort, TcpIpConnectionCallbackInterface pCallback) throws IOException,GeneralSecurityException,IncompatibleConnectionException {
		return TcpIpClient.connect(TcpIpConnectionTypes.PLAIN, pHost, pPort, pCallback);
	}

	/**
	 * Constructor for creating a connection to a server.
	 * @param pConnectionType type of connection. <a href="TcpIpConnectionTypes.html">TcpIpConnectionTypes</a> 
	 * @param pHost Ip host address, either a IP host number (xxx.xxx.xxx.xxx) or a host name (foo.bar.com)
	 * @param pPort the tcp/ip port on which the server will accept inbound connections on 
	 * @param pCallback interface to be called when messages from the server has been read or to signal communication error
	 * @return handle to the connection
	 * @throws IOException  error exception when failing to create and establish connection
	 * @throws IncompatibleConnectionException thrown when connecting to a server being started to accept s specific
	 * features i.e. plain, compressed, encrypted or eompressed_encrypted connections.
	 */
	public static TcpIpConnectionInterface connect(int pConnectionType,
			String pPrivateKeyFilename, String pKeyFilePassword,
			String pHost, int pPort, TcpIpConnectionCallbackInterface pCallback) throws IOException,IncompatibleConnectionException, GeneralSecurityException
	{
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

			Signer mSigner = (pPrivateKeyFilename != null) ? new Signer( pPrivateKeyFilename, pKeyFilePassword) : null;


			SocketAddress tEndpointAddress = new InetSocketAddress(pHost, pPort);
			Socket tSocket = new Socket();
			tSocket.connect(tEndpointAddress, 10000); // 10 seconds timeout

		TcpIpConnection tTcpIpConnection =  new TcpIpConnection(
				tSocket,
				mSigner,
				TcpIpConnection.ConnectionDirection.Out, 
				pConnectionType, 
				pCallback);

			tTcpIpConnection.init();
			tTcpIpConnection.start();

        return tTcpIpConnection;
	}

	public static TcpIpConnectionInterface connect(int pConnectionType,
												   String pHost, int pPort, TcpIpConnectionCallbackInterface pCallback) throws IOException,IncompatibleConnectionException, GeneralSecurityException
	{
		return TcpIpClient.connect( pConnectionType, null, null, pHost, pPort, pCallback );
	}
}
