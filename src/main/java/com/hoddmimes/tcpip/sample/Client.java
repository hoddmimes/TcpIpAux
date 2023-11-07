package com.hoddmimes.tcpip.sample;

import com.hoddmimes.tcpip.TcpIpClient;
import com.hoddmimes.tcpip.TcpIpConnectionCallbackInterface;
import com.hoddmimes.tcpip.TcpIpConnectionInterface;
import com.hoddmimes.tcpip.TcpIpConnectionTypes;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

public class Client implements TcpIpConnectionCallbackInterface
{
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private int mMinMsgSize = 128;
    private int mMaxMsgSize = 2048;
    private int mMsgsToSent = 10000;
    private int mCharacterSpan = 'Z' - 'A';


    private volatile int mMsgSent;
    private volatile long mBytesSent;
    private volatile long mStartTime;

    private volatile String mMessage;

    private boolean mVerifyMessage = true;

    private TcpIpConnectionInterface mClient;
    private final Random mRandom;
    private String mHost = "localhost";
    private int mPort = 9393;
    private TcpIpConnectionTypes mConnectionType = TcpIpConnectionTypes.Plain;
    private final NumberFormat mNumFmt;

    public static void main( String[] pArgs) {
        Client c = new Client();
        c.parseParameters( pArgs );
        c.connectToServer();
        c.startBounceData();
    }

    public Client() {
        mRandom = new Random();
        mNumFmt = NumberFormat.getNumberInstance();
        mNumFmt.setMaximumFractionDigits(2);
        mNumFmt.setMinimumFractionDigits(2);
    }

    private void log( String pMsg ) {
        System.out.println( SDF.format( System.currentTimeMillis()) + "  " + pMsg);
    }

    private void parseParameters( String[] pArgs ) {
        int i = 0;
        boolean tPlain = false, tCompression = false, tEncrypt = false;

        while( i < pArgs.length ) {
            if (pArgs[i].equalsIgnoreCase("-verify")) {
                mVerifyMessage = Boolean.parseBoolean( pArgs[i+1]);
                i++;
            }

            if (pArgs[i].equalsIgnoreCase("-minSize")) {
                mMinMsgSize = Integer.parseInt( pArgs[i+1]);
                i++;
            }
            if (pArgs[i].equalsIgnoreCase("-maxSize")) {
                mMaxMsgSize = Integer.parseInt( pArgs[i+1]);
                i++;
            }
            if (pArgs[i].equalsIgnoreCase("-count")) {
                mMsgsToSent = Integer.parseInt( pArgs[i+1]);
                i++;
            }
            if (pArgs[i].equalsIgnoreCase("-charSpan")) {
                mCharacterSpan = Integer.parseInt( pArgs[i+1]);
                i++;
            }

            if (pArgs[i].equalsIgnoreCase("-port")) {
                mPort = Integer.parseInt( pArgs[i+1]);
                i++;
            }
            if (pArgs[i].equalsIgnoreCase("-host")) {
                mHost = pArgs[i+1];
                i++;
            }
            if (pArgs[i].equalsIgnoreCase("-compress")) {
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
        if (mMinMsgSize > mMaxMsgSize) {
            System.out.println("-minSize must <=  -maxSize");
            System.exit(0);
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

    private void connectToServer() {
        try {
            mClient = TcpIpClient.connect( mConnectionType, mHost, mPort, this );
            log("Sucessfully connected to " + mHost + " on port " + mPort );
        }
        catch( Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isCompressed() {
        if ((mConnectionType == TcpIpConnectionTypes.Compression_Encrypt) || (mConnectionType == TcpIpConnectionTypes.Compression)) {
            return true;
        }
        return false;
    }

    private String buildMessage() {


        int tSize = mMinMsgSize + mRandom.nextInt( mMaxMsgSize - mMinMsgSize);
        StringBuilder sb = new StringBuilder( mMaxMsgSize );
        while( sb.length() < tSize ) {
            sb.append( getWord( 5,10, true));
        }
        if (sb.length() > tSize) {
            return sb.substring(0, tSize);
        }
        return sb.toString();

    }

    private void startBounceData()
    {
        mStartTime = System.nanoTime();
        mMsgSent = 0;
        mMessage = buildMessage();

        try {
            //System.out.println("Sending message: " + mMessage.length());
            mClient.write(mMessage.getBytes(), true);
        }
        catch( IOException e) {
            e.printStackTrace();
        }
    }


    private String getWord( int pMinLength, int pMaxLength, boolean pTailingSpace  ) {
        int tSize = pMinLength + mRandom.nextInt( pMaxLength - pMinLength );
        byte[] tBytes = (pTailingSpace) ? new byte[ tSize + 1 ] : new byte[ tSize ];
        for( int i = 0; i < tSize; i++ ) {
            tBytes[i] = (byte) (65  + mRandom.nextInt( mCharacterSpan ));
        }
        if (pTailingSpace) {
            tBytes[tSize] = (byte) 32;
        }
        return new String( tBytes );
    }

    @Override
    public void tcpipMessageRead(TcpIpConnectionInterface pConnection, byte[] pBuffer) {
        mMsgSent++;
        mBytesSent += pBuffer.length;
        if (mMsgSent == mMsgsToSent) {
            long uSec = (System.nanoTime() - mStartTime) / 1000L;
            long msg_sec = (mMsgSent * 1000_000L) / uSec;
            long kbytes_sec = (mBytesSent * 1000_000L) / (uSec * 1000);
            long usec_bounce = uSec / mMsgSent;

            log( String.format("All %d, msgs/sec: %d,   kb/sec: %d,     %d usec per bounce (xta/rcv)",
                        mMsgSent,  msg_sec, kbytes_sec, usec_bounce));

            pConnection.close();
            System.exit(0);
        }

        if ((mMsgSent % 5000) == 0) {
            if (isCompressed()) {
                log(" -- Messages sent " + mMsgSent
                        + " out compression rate " + mNumFmt.format(100.0d * pConnection.getOutputCompressionRate())
                        + "% in compression rate " + mNumFmt.format(100.0d * pConnection.getInputCompressionRate()) + "%");
            } else {
                log(" -- Messages sent " + mMsgSent);
            }
        }


        if (mVerifyMessage) {
            String tRcvMessage = new String( pBuffer );
            if (tRcvMessage.compareTo( mMessage ) != 0) {
                log("Sent data not equal with received data [ Message: " + mMsgSent + " bytes sent: " + mBytesSent );
            }
        }
        mMessage = buildMessage();
        try {
            //System.out.println("Sending message: " + mMessage.length());
            pConnection.write(mMessage.getBytes(), true);
        }
        catch( IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tcpipClientError(TcpIpConnectionInterface pConnection, Exception e) {
        log("disconnected, error: " + e.getMessage());
        e.printStackTrace();
    }
}
