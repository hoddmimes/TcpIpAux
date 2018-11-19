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
    private TcpIpConnectionTypes mConnectionType;
    private volatile boolean mTrace;

    public NetworkClient() {
        mCompleteSemaphore = new Object();
        mRandom = new Random();
        mException = null;
        mTrace = false;

    }

    public Object test(TcpIpConnectionTypes pConnectionType) {
        return test(pConnectionType, 10000, 1, 20000, (int) ('Z' - 'A'), false);
    }

    public Object test(TcpIpConnectionTypes pConnectionType, boolean pTrace) {
        return test(pConnectionType, 10000, 1, 20000, (int) ('Z' - 'A'), pTrace);
    }


    public Object test(TcpIpConnectionTypes pConnectionType, int pMsgToSend, int pMsgMinSize, int pMsgMaxSize, int pCharacterSpan, boolean pTrace) {
        mTrace = pTrace;
        mConnectionType = pConnectionType;
        mMsgToSend = pMsgToSend;
        mMsgMaxSize = pMsgMaxSize;
        mMsgMinSize = pMsgMinSize;
        mCharacterSpan = pCharacterSpan;
        try {
            mConnection = TcpIpClient.connect(pConnectionType, "localhost", 9393, this);
            bounceData();
        } catch (Exception e) {
            mException = e;
        }
        return this.mException;
    }

    private boolean isCompressed() {
        if ((TcpIpConnectionTypes.Compression == mConnectionType) || (TcpIpConnectionTypes.Compression_Encrypt == mConnectionType)) {
            return true;
        }
        return false;
    }

    private void bounceData() {
        try {
            mMsgSent = 0;
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


    @Override
    public void tcpipMessageRead(TcpIpConnectionInterface pConnection, byte[] pBuffer) {
        mMsgSent++;
        if (mTrace) {
            System.out.println("[Client] Msg sent: " + mMsgSent + " size: " + pBuffer.length);
        }
        if (mMsgSent == mMsgToSend) {
            if (isCompressed()) {
                NumberFormat nf = NumberFormat.getNumberInstance();
                nf.setMinimumFractionDigits(2);
                nf.setMaximumFractionDigits(2);
                System.out.println("  in compression rate: " + nf.format( 100.0d * pConnection.getInputCompressionRate()) +
                                   "%  out compression rate: " + nf.format( 100.0d * pConnection.getOutputCompressionRate()) + "%");
            }
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