package com.hoddmimes.tcpip;

import com.hoddmimes.tcpip.crypto.DH;
import com.hoddmimes.tcpip.crypto.Crypto;
import com.hoddmimes.tcpip.impl.*;
import com.hoddmimes.tcpip.messages.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 
 *
 */
 class TcpIpConnection extends Thread implements TcpIpConnectionInterface, TcpIpCountingInterface
{
	static enum ConnectionDirection {In, Out};

	private int STREAM_BUFFER_SIZE = (16 * 1024); // 16K

	private static final int MAGICAL_SIGN = 0x504f4242;
	private static final int CRYPTO_HEADER_SIZE = 3 * Integer.BYTES; // MagicSign + Msg Length on the net (padding inckluded) + True User Data Length

	private static SimpleDateFormat cSDF = new SimpleDateFormat("HH:mm:ss.SSS");
	private static AtomicInteger cThreadIndex = new AtomicInteger(0);

	private TcpIpConnectionCallbackInterface mCallback;
	private Socket mSocket;
	private volatile boolean mClosedCalled;
	private int mIndex;
	private Date mConnectionTime;
	private Object mUserCntx;
	private TcpIpConnectionTypes mConnectionType;
	private ConnectionDirection mConnectionDirection;
	private boolean mClientServerCompatible;

	private volatile DataInputStream mIn;
	private volatile DataOutputStream mOut;

	private volatile ByteCountingOutputStream mUnCompStatOutputStream = null;
	private volatile ByteCountingOutputStream mCompStatOutputStream = null;
	private volatile ByteCountingInputStream mUnCompStatInputStream = null;
	private volatile ByteCountingInputStream mCompStatInputStream = null;

	private Crypto 	mEncrypt;
	private Crypto mDecrypt;

	private ByteBuffer mCryptoBuffer;



	TcpIpConnection( Socket pSocket, 
			ConnectionDirection pConnectionDirection, 
			TcpIpConnectionTypes pConnectionType, 
			TcpIpConnectionCallbackInterface pCallback) {
		mSocket = pSocket;
		mConnectionDirection = pConnectionDirection;
		mCallback = pCallback;
		mClosedCalled = false;
		mConnectionType = pConnectionType;
		mClientServerCompatible = true; // let us assume that we are compatible i.e. version and connection type

		if ((pConnectionType != null) && (pConnectionType.equals(TcpIpConnectionTypes.Encrypt) || pConnectionType.equals(TcpIpConnectionTypes.Compression_Encrypt))) {
			adjustCryptoBuffers( 1024 );
		}

		mConnectionTime = new Date();
		mIndex = cThreadIndex.incrementAndGet();
		try {
			mSocket.setTcpNoDelay(true);
			mSocket.setKeepAlive(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void adjustCryptoBuffers( int pSize  ) {
		if (mCryptoBuffer == null) {
			int tSize = Math.max( pSize, 1024);
			mCryptoBuffer = ByteBuffer.allocate( tSize );
		} else if (pSize > mCryptoBuffer.capacity()) {
			mCryptoBuffer = ByteBuffer.allocate( pSize );
		} else if ((pSize < mCryptoBuffer.capacity()) && ( mCryptoBuffer.capacity() > 2048)) {
			mCryptoBuffer = ByteBuffer.allocate( pSize );
		}
		mCryptoBuffer.clear();
	}

	private void trace( String pText ) {
		//System.out.println("[" + mConnectionDirection +"] " + pText + " thread: " + this.getName());
	}

	protected  void init() throws IOException, IncompatibleConnectionException
	{
		// This class is used by servers and clients to wrap the transport stack.
		// Server, may be configured to force a protocol stack (i.e. compressin and/or encryption) or accept whatever the client chose
		// If the server initialize the TcpIpConnection with a ConnectionType (i.e. not null) it must match the client connection type in the initial message
		// If connection type is not set the server will adopt to whatever the client has chosen.


		DH tDHData = (isClient()) ? synchronizeParametersWithServer() : synchronizeParametersWithClient();

		// If encryption requested
		if (isEncrypted()) {
			mDecrypt = new Crypto(false, tDHData.getSharedSecretKey());
			mEncrypt = new Crypto(true,tDHData.getSharedSecretKey());
			tDHData = null;
		}

		mIn = new DataInputStream(mSocket.getInputStream());
		mOut =  new DataOutputStream(mSocket.getOutputStream());

		// If Compression requested

		if (isCompressed()) {

			mCompStatInputStream = new ByteCountingInputStream( mIn );
			mCompStatOutputStream = new ByteCountingOutputStream( mOut );

			mUnCompStatOutputStream = new ByteCountingOutputStream( new BufferedOutputStream(new CompressionOutputStream(mCompStatOutputStream), STREAM_BUFFER_SIZE));
			mUnCompStatInputStream = new ByteCountingInputStream( new BufferedInputStream( new CompressionInputStream(mCompStatInputStream), STREAM_BUFFER_SIZE));

			mIn = new DataInputStream(mUnCompStatInputStream);
			mOut = new DataOutputStream(mUnCompStatOutputStream);
		} else {
			mUnCompStatOutputStream = new ByteCountingOutputStream( new DataOutputStream(mOut));
			mUnCompStatInputStream = new ByteCountingInputStream( new DataInputStream(mIn));

			mIn = new DataInputStream(mUnCompStatInputStream);
			mOut = new DataOutputStream(mUnCompStatOutputStream);
		}
	}



	private DH synchronizeParametersWithServer() throws IOException, IncompatibleConnectionException
	{
		DH tDHKeys = null;

		if (isEncrypted())
		{
			BigInteger P = DH.DEFAULT_PRIME;
			BigInteger G = DH.DEFAULT_G;
			tDHKeys = new DH( G, P ); //
		}

		InitMsgRqst tRqst = (isEncrypted()) ? new InitMsgRqst( mConnectionType, tDHKeys.getG(), tDHKeys.getP(), tDHKeys.getPublicKey()) : new InitMsgRqst( mConnectionType );

		byte[] tNetMsg = tRqst.encode();

		DataInputStream tIn = new DataInputStream( mSocket.getInputStream());
		DataOutputStream tOut = new DataOutputStream( mSocket.getOutputStream());

		// Send init request
		tOut.writeInt(tNetMsg.length);
		tOut.write( tNetMsg );
		tOut.flush();

		int tSize = tIn.readInt();
		byte[] tBuffer = new byte[ tSize ];
		tIn.readFully( tBuffer );

		InitMsgRsp tRsp =  new InitMsgRsp( new MessageDecoder( tBuffer ));
		if (!tRsp.isAccepted()) {
			throw new IncompatibleConnectionException(tRsp.getErrorText());
		}
		if (isEncrypted()) {
			tDHKeys.calculateSharedKey( tRsp.getClientPubKey());
		}
		return tDHKeys;
	}

	private DH synchronizeParametersWithClient() throws IOException, IncompatibleConnectionException {
		boolean tAccepted = true;
		String tErrorText = null;
		DH tDHData = null;


		DataInputStream tIn = new DataInputStream(mSocket.getInputStream());
		DataOutputStream tOut = new DataOutputStream(mSocket.getOutputStream());

		int tSize = tIn.readInt();
		if ((tSize <= 0) && (tSize >= 4096)) {
			throw new IOException("tcpIpAux: Most likely an intial garbage message");
		}
		byte[] tBuffer = new byte[tSize];
		tIn.readFully(tBuffer);
		InitMsgRqst tInitMsgRqst = new InitMsgRqst(new MessageDecoder(tBuffer));

		// Check version
		if (tInitMsgRqst.getVersion() != Version.VERSION) {
			tAccepted = false;
			tErrorText = "Client/Server versions are incompatible";
		} else {
			// Check connection type
			if (this.mConnectionType == null) {
				// server accepts whatever client chose
				this.mConnectionType = tInitMsgRqst.getConnectionType();
			} else if (this.mConnectionType != tInitMsgRqst.getConnectionType()) {
				tAccepted = false;
				tErrorText = "Client/Server incompatible feature stacks";
			}
		}

		InitMsgRsp tResponse = null;
		if (isEncrypted()) {
			tDHData = new DH(tInitMsgRqst.getCryptG(), tInitMsgRqst.getCryptP());
			tDHData.calculateSharedKey(tInitMsgRqst.getClientPubKey());
			tResponse =  new InitMsgRsp( tDHData.getPublicKey(), tAccepted, tErrorText);
		} else {
			tResponse =  new InitMsgRsp( null, tAccepted, tErrorText);
		}

		byte[] tNetRspMsg = tResponse.encode();
		tOut.writeInt( tNetRspMsg.length );
		tOut.write( tNetRspMsg );
		tOut.flush();

		if (!tAccepted) {
			throw new IncompatibleClassChangeError(tErrorText);
		}
		return tDHData;
	}






	private boolean isClient() {
		return (mConnectionDirection == ConnectionDirection.Out);
	}

	private boolean isServer() {
		return (mConnectionDirection == ConnectionDirection.In);
	}

	private boolean isCompressed() {
		if ((mConnectionType == TcpIpConnectionTypes.Compression) || (mConnectionType == TcpIpConnectionTypes.Compression_Encrypt)) {
			return true;
		}
		return false;
	}

	private boolean isEncrypted() {
		if ((mConnectionType == TcpIpConnectionTypes.Encrypt) || (mConnectionType == TcpIpConnectionTypes.Compression_Encrypt)) {
			return true;
		}
		return false;
	}

	@Override
	public void setUserCntx(Object pObject) {
		mUserCntx = pObject;
	}

	@Override
	public Object getUserCntx() {
		return mUserCntx;
	}

	@Override
	public void close() {
		synchronized (this) {
			mClosedCalled = true;
		}
		synchronized (this) {
			try {
				mIn.close();
				mOut.close();
				mSocket.close();
				if (mEncrypt != null) {
					mEncrypt = null;
				}
				if (mDecrypt != null) {
					mDecrypt = null;
				}

			} catch (IOException e) {
			}
		}

	}

	public void flush() throws IOException {
		mOut.flush();
	}

	public TcpIpConnectionCallbackInterface getCallbackInterface() {
		return this.mCallback;
	}

	public String getRemoteHost() {
		return mSocket.getRemoteSocketAddress().toString();
	}

	public int getRemotePort() {
		return mSocket.getPort();
	}

	public void write(byte[] pBuffer) throws IOException {
			write(pBuffer,0,pBuffer.length,true);
	}

	public void write(byte[] pBuffer, int pOffset, int pLength) throws IOException 
	{
		write(pBuffer,pOffset,pLength,true);
	}
	
	
	
	public void write(byte[] pBuffer, int pOffset, int pLength, boolean pFlushFlag) throws IOException 
	{

		synchronized (this) {
			if (isEncrypted()) {
				adjustCryptoBuffers( pLength + CRYPTO_HEADER_SIZE + mEncrypt.getBlockSize());

                mCryptoBuffer.clear();
				mCryptoBuffer.putInt(MAGICAL_SIGN);
				mCryptoBuffer.putInt(0); // Put an entry for the length field, updated later
				mEncrypt.encrypt( pBuffer, pOffset, mCryptoBuffer, pLength);

				//Update the total message length. It is the length of the mCryptoBuffer
				// minus the length for the magical sign and the length field itself.
				int tSize = mCryptoBuffer.position() - Integer.BYTES - Integer.BYTES;
				mCryptoBuffer.putInt(Integer.BYTES, tSize );
                mOut.write(mCryptoBuffer.array(), 0, mCryptoBuffer.position());

			} else {
				mOut.writeInt(MAGICAL_SIGN);
				mOut.writeInt(pLength);
				mOut.write(pBuffer, pOffset, pLength);
			}
			if (pFlushFlag) {
				mOut.flush();
            }
		}
	}
	public void write(byte[] pBuffer, boolean pFlushFlag) throws IOException {
		write( pBuffer,0,pBuffer.length, pFlushFlag );
	}

	@Override
	public String toString() {
		return this.getConnectionInfo();
	}

	public String getConnectionInfo() {
		return "[host: " + mSocket.getRemoteSocketAddress().toString()
				+ " direction: " + this.mConnectionDirection.toString()
				+ " port: " + mSocket.getPort() + " conn id: " + mIndex
				+ " conn time: " + cSDF.format(mConnectionTime) + "]";
	}

	@Override
	public void run() {
		byte[] tBuffer;
		int tSize;

		this.setName("TCPIP thread " + mIndex);

		// The init with its setup must execute before any user data can be process in the loop below.
		// TcpIp clients will execute the init method synchronously as part of the TcpIpConnection creation see
		// class TcpIpClient constructor
		// For server we do not the init to execute in the context of the server accept thread
		// there fore we inwoke the init here for server connections.

		if (isServer()) {
			try {init();}
			catch( Exception e) {
				if (!mClosedCalled) {
					mCallback.tcpipClientError(this, e);
				}
			}
		}


		while (!mClosedCalled) {
			try {

				int tMagicalSign = mIn.readInt();

				if (tMagicalSign != MAGICAL_SIGN) {
					if (!mClosedCalled) {
						mCallback.tcpipClientError(this, new IOException("Read message with invalid magic sign"));
					}
					return;
				}


				tSize = mIn.readInt();
				tBuffer = new byte[tSize];
				mIn.readFully(tBuffer);

				if (isEncrypted()) {
					adjustCryptoBuffers( tSize );
					tBuffer = mDecrypt.decrypt( tBuffer );
				}
				mCallback.tcpipMessageRead(this, tBuffer);

			} catch (IOException e) {

				if (!mClosedCalled) {
					mCallback.tcpipClientError(this, e);
				}
				return;
			}
		}
	}

	@Override
	public long getBytesRead() {
		return this.mUnCompStatInputStream.getBytesRead();
	}

	@Override
	public long getBytesWritten() {
		return this.mUnCompStatOutputStream.getBytesWritten();
	}

	@Override
	public double getInputCompressionRate() {
		if (isCompressed()) {

			double tRatio = (((double) mUnCompStatInputStream.getBytesRead() - (double) mCompStatInputStream
					.getBytesRead()) / mUnCompStatInputStream.getBytesRead());
			return tRatio;
		}
		return 0D;
	}

	@Override
	public double getOutputCompressionRate() {
		if (isCompressed()) {

			double tRatio = (((double) mUnCompStatOutputStream.getBytesWritten() - (double) mCompStatOutputStream .getBytesWritten()) / 
					mUnCompStatOutputStream.getBytesWritten());
			return tRatio;
		}

		return 0D;
	}
}
