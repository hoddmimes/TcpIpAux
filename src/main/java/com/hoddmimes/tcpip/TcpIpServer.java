package com.hoddmimes.tcpip;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
//import java.security.cert.Certificate;
//import java.util.ArrayList;
//import java.util.Enumeration;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


/**
 * Class for establishing a TCP/IP server listener.
 */
public class TcpIpServer extends Thread {

	private int mAcceptPort;
	private TcpIpServerCallbackInterface mCallback;
	private ServerSocket mServerSocket;
	private TcpIpConnectionTypes mConnectionType;
	private volatile boolean mCloseFlag;



	
	/**
	 * Establish and start server listener using plain tcp/ip i.e. no compression or encryption. 
	 * @param pAcceptPort tcp/ip port to accept connection on 
	 * @param pCallback callback interface to be invoked when receiving inbound connection, messages read and fatal error notifications 
	 * @throws IOException exception signaled in case fatal error occurs when establishing and starting server
	 */
	public TcpIpServer(int pAcceptPort, TcpIpServerCallbackInterface pCallback) throws IOException {
		this(null, pAcceptPort, null, pCallback);
	}

	/**
	 * Establish and start server listener. Optional to start server with compression and/or encryption
	 * @param pConnectionType type of session @see {@link TcpIpConnectionTypes}. <i><b>Note!</b> if passing null as parameter
	 *                        the server will will not verify incomming client connection. The server will adopt to whatever
	 *                        the client connection request, plain,copression and/or encryption</i> If a connection type is
	 *                        specified the server will verify that the incomming connection are initiated/started
	 *                        with same features.
	 * @param pAcceptPort tcp/ip port to accept connection on 
	 * @param pAcceptInterface the address of the network interface through which connections will be accepted. If being null the server will accept connection on ADDR_ANY interface
	 * @param pCallback callback interface to be invoked when receiving inbound connection, messages read and fatal error notifications 
	 * @throws IOException exception signaled in case fatal error occurs when establishing and starting server
	 */
	public TcpIpServer(TcpIpConnectionTypes pConnectionType, int pAcceptPort,
			String pAcceptInterface, TcpIpServerCallbackInterface pCallback) throws IOException {
		mConnectionType = pConnectionType;
		mAcceptPort = pAcceptPort;
		mCallback = pCallback;
		mCloseFlag = false;
		
		InetAddress tAcceptInterface = (pAcceptInterface == null) ? InetAddress.getByName("0.0.0.0") : InetAddress.getByName(pAcceptInterface);
		mServerSocket = new ServerSocket(mAcceptPort,20,tAcceptInterface);

		// Start accept thread
		start();
	}

	/**
	 * Close the server socket and stops accepting connection
	 */
	public void close() {
		if (mServerSocket != null) {
			mCloseFlag = true;
			try {mServerSocket.close();}
			catch( IOException e) {}
		}
	}

	/**
	 * Background thread accepting inbound connection
	 */
	@Override
	public void run() {
		if (mConnectionType != null) {
			setName("TcpIp Server [" + mConnectionType.toString() + "] on port " + mAcceptPort);
		} else {
			setName("TcpIp Server [ Any Type ] on port " + mAcceptPort);
		}
		while (!mCloseFlag) {
			try {
				Socket tSocket = mServerSocket.accept();


				TcpIpConnection tConnection = new TcpIpConnection(tSocket,
											TcpIpConnection.ConnectionDirection.In,
											mConnectionType, 
											mCallback);
				
				mCallback.tcpipInboundConnection(tConnection);
                tConnection.start();
			} catch (Exception e) {
				if (!mCloseFlag) {
					e.printStackTrace();
				}
			}
		}
	}

	
}
