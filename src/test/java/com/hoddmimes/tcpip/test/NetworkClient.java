package com.hoddmimes.tcpip.test;

import com.hoddmimes.tcpip.TcpIpClient;
import com.hoddmimes.tcpip.TcpIpConnectionCallbackInterface;
import com.hoddmimes.tcpip.TcpIpConnectionInterface;
import com.hoddmimes.tcpip.TcpIpConnectionTypes;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Random;

public class NetworkClient implements TcpIpConnectionCallbackInterface {

    private TcpIpConnectionInterface mConnection;
    private Object mCompleteSemaphore;
    private int mMsgSent = 10000;
    private int mMsgToSend = 10000;
    private int mMsgMinSize = 1;
    private int mMsgMaxSize = 20000;
    private int mCharacterSpan = 'Z' - 'A'; // Span is 25
    private Random mRandom;
    private Exception mException;
    private byte[] mCurrentMessage;
    private int mConnectionType;
    private volatile boolean mTrace;
    private long mStartTime;
    private long mBytesSent;
    private long mLastNS = 0L;

    public NetworkClient() {
        mCompleteSemaphore = new Object();
        mRandom = new Random();
        mException = null;
        mTrace = false;

    }

    public Object test(int pConnectionType) {
        return test(pConnectionType, null, null, 10000, 1, 20000, (int) ('Z' - 'A'), false);
    }

    public Object test(int pConnectionType, boolean pTrace) {
        return test(pConnectionType, null, null, 10000, 1, 20000, (int) ('Z' - 'A'), pTrace);
    }

    public Object test(int pConnectionType, int pMsgToSend, int pMsgMinSize, int pMsgMaxSize, int pCharacterSpan, boolean pTrace) {
        return test( pConnectionType, null, null, pMsgToSend, pMsgMinSize, pMsgMaxSize,  pCharacterSpan, pTrace);
    }

    public Object test(int pConnectionType, String pPrivateKeyFilename, String pKeyFilePassword, int pMsgToSend, int pMsgMinSize, int pMsgMaxSize, int pCharacterSpan, boolean pTrace) {
        mTrace = pTrace;
        mConnectionType = pConnectionType;
        mMsgToSend = pMsgToSend;
        mMsgMaxSize = pMsgMaxSize;
        mMsgMinSize = pMsgMinSize;
        mCharacterSpan = pCharacterSpan;
        mException = null;
        try {
            mConnection = TcpIpClient.connect(pConnectionType, pPrivateKeyFilename, pKeyFilePassword, "localhost", 9393, this);
            bounceData();
        } catch (Exception e) {
            mException = e;
        }
        return this.mException;
    }

    private boolean isCompressed() {
        if ((mConnectionType & TcpIpConnectionTypes.COMPRESS) != 0) {
            return true;
        }
        return false;
    }

    private void bounceData() {
        try {
            mMsgSent = 0;
            mStartTime = System.nanoTime();
            mBytesSent = 0L;

            mCurrentMessage = buildMessage();
            synchronized (mCompleteSemaphore) {
                mConnection.write(mCurrentMessage, true);
                try {
                    mCompleteSemaphore.wait();
                } catch (InterruptedException e) { }
            }
        } catch (Exception e) {
            this.mException = e;
        }
    }

    private byte[] buildMessage() {
        int tSize = mMsgMinSize + mRandom.nextInt(mMsgMaxSize - mMsgMinSize);
        byte[] tBytes = new byte[tSize];
        for (int i = 0; i < tSize; i++) {
            tBytes[i] = (byte) ((65 + mRandom.nextInt(mCharacterSpan)) &0xFF);
        }
        return tBytes;
    }

    private long usecDelta() {
        if (mLastNS == 0) {
            mLastNS = System.nanoTime();
            return 0;
        }
        long nowNS = System.nanoTime();
        long usec = (nowNS - mLastNS) / 1000L;
        mLastNS = nowNS;
        return usec;
    }

    @Override
    public void tcpipMessageRead(TcpIpConnectionInterface pConnection, byte[] pBuffer) {
        mMsgSent++;
        mBytesSent += pBuffer.length;

        if (mTrace) {
            System.out.println( usecDelta() + " usec  [Client] Msg sent: " + mMsgSent + " size bounced: " + mBytesSent);
        }
        if (mMsgSent == mMsgToSend) {
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);

            long tRoundTripUsec = ((System.nanoTime() - mStartTime)  / 1000L) / mMsgSent;
            double kb_sec = (double) mBytesSent / (double) ((System.nanoTime() - mStartTime) /1000L);
            if (isCompressed()) {
                System.out.println("  in compression rate: " + nf.format( 100.0d * pConnection.getInputCompressionRate()) +
                                   "%  out compression rate: " + nf.format( 100.0d * pConnection.getOutputCompressionRate()) + "%");
            }
            System.out.println("All " + mMsgSent + " roundtrip: " + tRoundTripUsec + " usec  Kb/sec " + nf.format(kb_sec));
            closeAndNotify(pConnection, null);
        } else {
            if (!compareByteArrays(pBuffer, mCurrentMessage)) {
                if (mTrace) {
                    System.out.println("[Client] Bounced message (" + mMsgSent + ") is not same" );
                }
                closeAndNotify(pConnection, new IOException("Bounced message (" + mMsgSent + ") is not same"));
            } else {
                mCurrentMessage = buildMessage();
                try {
                    if (mTrace) {
                        System.out.println("[Client] Bouncing new message (" + (mMsgSent+1) + ") size: " +  mCurrentMessage.length );
                    }
                    pConnection.write(mCurrentMessage, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    closeAndNotify(pConnection, e);
                }
            }
        }
    }

    private void closeAndNotify(TcpIpConnectionInterface pConnection, Exception e) {
        pConnection.close();
        synchronized (mCompleteSemaphore) {
            mCompleteSemaphore.notifyAll();
        }
    }


    private boolean compareByteArrays(byte[] a, byte[] b) {
        if (a == null && b == null) {
            return true;
        }
        if (a.length != b.length) {
            return false;
        }

        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void tcpipClientError(TcpIpConnectionInterface pConnection, Exception e) {
        closeAndNotify(pConnection, e);
    }
    }