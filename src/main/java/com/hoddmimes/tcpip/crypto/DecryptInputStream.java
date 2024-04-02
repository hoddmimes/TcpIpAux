package com.hoddmimes.tcpip.crypto;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.security.GeneralSecurityException;


public class DecryptInputStream extends FilterInputStream {
    private DataInputStream mTcpInStream;  // Tcp/ip in stream
    ByteBuffer              mData;
    private Cipher          mCipher;


    public DecryptInputStream(InputStream pTcpIpSocketInputStream, Cipher pCipher) throws IOException {
        super(pTcpIpSocketInputStream);
        mTcpInStream = new DataInputStream(pTcpIpSocketInputStream);
        mCipher = pCipher;
        mData = null;

    }



    private void readAndDecryptData() throws IOException
    {

       int tMsgSize = mTcpInStream.readInt();

       // Read the payload
        byte tEncryptedBuffer[] = new byte[tMsgSize];
        mTcpInStream.readFully(tEncryptedBuffer);

        try {
            byte[] tDecryptedData = mCipher.doFinal( tEncryptedBuffer );
            mData = ByteBuffer.wrap( tDecryptedData );
        }
        catch( GeneralSecurityException e) {
            throw new IOException(e);
        }
    }


    private void processReadRequest( int pReadSize ) throws IOException {
        if ((mData == null) || (mData.remaining()== 0)) {
            readAndDecryptData();
        }
    }



    @Override
    public int  read( byte[] pBuffer ) throws IOException {
        return read(pBuffer,0,pBuffer.length);
    }


    @Override
    public int read( byte[] pBuffer, int pOffset, int pLength  ) throws IOException
    {
        processReadRequest(pLength);
        int tSize = Math.min( pLength, mData.remaining());
        mData.get( pBuffer, pOffset, tSize);
        return tSize;

    }

    @Override
    public int read() throws IOException
    {
        processReadRequest(Byte.BYTES);
        return (mData.get() & 0xFF);
    }
}