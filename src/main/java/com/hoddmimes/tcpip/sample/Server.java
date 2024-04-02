package com.hoddmimes.tcpip.sample;

import com.hoddmimes.tcpip.*;

import java.io.EOFException;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class Server implements TcpIpConnectionCallbackInterface, TcpIpServerCallbackInterface
{
    private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private int mConnectionType = 0;
    private int mPort = 9393;
    private TcpIpServer mServer;

    private void parseParameters( String[] pArgs ) {
        int i = 0;

        while( i < pArgs.length ) {
            if (pArgs[i].equalsIgnoreCase("-port")) {
               mPort = Integer.parseInt( pArgs[i+1]);
               i++;
            }
            if (pArgs[i].equalsIgnoreCase("-compression")) {
                mConnectionType += TcpIpConnectionTypes.COMPRESS;
            }
            if (pArgs[i].equalsIgnoreCase("-encrypt")) {
                mConnectionType += TcpIpConnectionTypes.ENCRYPT;
            }
            if (pArgs[i].equalsIgnoreCase("-plain")) {
                mConnectionType += TcpIpConnectionTypes.PLAIN;
            }
            if (pArgs[i].equalsIgnoreCase("-ssh-auth")) {
                mConnectionType += TcpIpConnectionTypes.SSH_SIGNING;
            }
            i++;
        }
        try{
            TcpIpConnectionTypes.validate( mConnectionType );
        }
        catch( IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void log( String pMsg ) {
        System.out.println( SDF.format( System.currentTimeMillis()) + "  " + pMsg);
    }

    private void startServer( )
    {
        try {
            mServer = new TcpIpServer( mConnectionType, "./", mPort, null, this );
        }
        catch( IOException e) {
          log("Failed to start server, error: " + e.getMessage());
           e.printStackTrace();
        }

        if (mConnectionType == 0) {
            log("Successfully started server, listning on port: " + mPort +
                    "\n   Connection type is not defined, client decides");
        } else {
            log("Successfully started server, listning on port: " + mPort +
                    "\n   connection types: " + TcpIpConnectionTypes.toString(mConnectionType));
        }
    }

    public static void main( String[] pArgs ) {
        Server s = new Server();
        s.parseParameters( pArgs );
        s.startServer();
    }

    @Override
    public void tcpipMessageRead(TcpIpConnectionInterface pConnection, byte[] pBuffer)
    {
        // Just echo the message back to client
        try {pConnection.write(pBuffer); }
        catch( IOException e ) {
            e.printStackTrace();
            pConnection.close();
        }
    }

    @Override
    public void tcpipClientError(TcpIpConnectionInterface pConnection, Exception e) {
        pConnection.close();
        if (e instanceof EOFException) {
            log("Client Error: " + pConnection + "\n    error: EOFException");
        } else {
            log("Client Error: " + pConnection +
                    "\n    error: " + e.getClass().getSimpleName() + " : " + e.getMessage());
        }
    }

    @Override
    public void tcpipInboundConnection(TcpIpConnectionInterface pConnection) {
        log("inbound connect: " + pConnection );
    }
}
