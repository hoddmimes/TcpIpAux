package com.hoddmimes.tcpip.sample;

import com.hoddmimes.tcpip.*;

import java.io.EOFException;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class Server implements TcpIpConnectionCallbackInterface, TcpIpServerCallbackInterface
{
    private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private TcpIpConnectionTypes mConnectionType = null;
    private int mPort = 9393;
    private TcpIpServer mServer;

    private void parseParameters( String[] pArgs ) {
        int i = 0;
        boolean tPlain = false, tCompression = false, tEncrypt = false;

        while( i < pArgs.length ) {
            if (pArgs[i].equalsIgnoreCase("-port")) {
               mPort = Integer.parseInt( pArgs[i+1]);
               i++;
            }
            if (pArgs[i].equalsIgnoreCase("-compression")) {
                tCompression = true;
            }
            if (pArgs[i].equalsIgnoreCase("-encrypt")) {
                tEncrypt = true;
            }
            if (pArgs[i].equalsIgnoreCase("-plain")) {
                tCompression = true;
            }
            i++;
        }
        if (tPlain && tCompression && tEncrypt) {
            System.out.println("Either -plain, -encrypt, -compression or -encrypt and -compression");
            System.exit(0);
        }

        if (tPlain) {
            mConnectionType = TcpIpConnectionTypes.Plain;
        } else if (tCompression && tEncrypt) {
            mConnectionType = TcpIpConnectionTypes.Compression_Encrypt;
        } else if (tCompression) {
            mConnectionType = TcpIpConnectionTypes.Compression;
        } else if (tEncrypt) {
            mConnectionType = TcpIpConnectionTypes.Encrypt;
        }
    }

    private void log( String pMsg ) {
        System.out.println( SDF.format( System.currentTimeMillis()) + "  " + pMsg);
    }

    private void startServer( )
    {
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
