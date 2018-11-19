package com.hoddmimes.tcpip.test;

import com.hoddmimes.tcpip.*;
import com.hoddmimes.tcpip.sample.Server;

import java.io.EOFException;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class NetworkServer implements TcpIpConnectionCallbackInterface, TcpIpServerCallbackInterface
{
    private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private TcpIpConnectionTypes mConnectionType = null;
    private int mPort = 9393;
    private TcpIpServer mServer;
    private volatile boolean mTrace;



    private void log( String pMsg ) {
        System.out.println( SDF.format( System.currentTimeMillis()) + "  " + pMsg);
    }

    void startServer() {
        startServer( false );
    }
    void startServer( boolean pTrace)
    {
        mTrace = pTrace;
        try {
            mServer = new TcpIpServer( mConnectionType, mPort, null, this );
        }
        catch( IOException e) {
            log("Failed to start server, error: " + e.getMessage());
            e.printStackTrace();
        }

        if (mConnectionType == null) {
            log("Sucessfully started server, listning on port: " + mPort +
                    "\n   Connection type is not defined, client decides");
        } else {
            log("Sucessfully started server, listning on port: " + mPort +
                    "\n   connection types: " + mConnectionType.toString() );
        }
    }

    NetworkServer( TcpIpConnectionTypes pConnectionType )
    {
        mConnectionType = pConnectionType;
    }


    public void close() {
        this.mServer.close();
    }

    @Override
    public void tcpipMessageRead(TcpIpConnectionInterface pConnection, byte[] pBuffer)
    {
        UserContext uc = (UserContext) pConnection.getUserCntx();
        if ((uc != null) && (mTrace)) {
            uc.update( pBuffer.length);
            System.out.println( uc );
        }

        // Just echo the message back to client
        try {pConnection.write(pBuffer,true); }
        catch( IOException e ) {
            e.printStackTrace();
            pConnection.close();
        }
    }

    @Override
    public void tcpipClientError(TcpIpConnectionInterface pConnection, Exception e) {
        pConnection.close();
        if (e instanceof EOFException) {
            log("Client disconnect: " + pConnection );
        } else {
            log("Client Error: " + pConnection +
                    "\n    error: " + e.getClass().getSimpleName() + " : " + e.getMessage());
        }
    }

    @Override
    public void tcpipInboundConnection(TcpIpConnectionInterface pConnection) {
        pConnection.setUserCntx( new UserContext());
        log("inbound connect: " + pConnection );
    }

    class UserContext {
        long mMessageReceived;
        long mLastMessageSize;
        long mBytesReceived;

        UserContext() {
            mMessageReceived = 0;
            mBytesReceived = 0;
            mLastMessageSize = 0;
        }

        void update( int pBytesReceived ) {
            mMessageReceived++;
            mLastMessageSize = pBytesReceived;
            mBytesReceived += pBytesReceived;
        }

        public String toString() {
            return "[Server] Msgs received: " + mMessageReceived + " last msg size: " + mLastMessageSize + " bytes received: " + mBytesReceived;
        }
    }
}

