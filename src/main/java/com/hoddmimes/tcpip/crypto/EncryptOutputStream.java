package com.hoddmimes.tcpip.crypto;

import javax.crypto.Cipher;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

public class EncryptOutputStream extends FilterOutputStream {
    private DynamicByteBuffer mBuffer;
    private DataOutputStream mOutStream;
    private Cipher mCipher;

    private boolean mClosed;



    public EncryptOutputStream(OutputStream pTcpIpSocketOutputStream, Cipher pCipher ) {
        super(pTcpIpSocketOutputStream);
        mOutStream = new DataOutputStream( pTcpIpSocketOutputStream );
        mBuffer = new DynamicByteBuffer( 4096 );
        mCipher = pCipher;
        mClosed = false;
    }

    @Override
    public void write( byte[] pBuffer ) throws IOException {
        if (mClosed) {
            throw new IOException("Encrypted output stream is closed");
        }
        encrypt( pBuffer, 0, pBuffer.length );
    }

    @Override
    public void write( byte[] pBuffer, int pOffset, int pLength  ) throws IOException
    {
        if (mClosed) {
            throw new IOException("Encrypted output stream is closed");
        }
        encrypt( pBuffer, pOffset, pLength );
    }

    @Override
    public void write(  int pByte ) throws IOException
    {
        if (mClosed) {
            throw new IOException("Encrypted output stream is closed");
        }
        encrypt( pByte );
    }

    @Override
    public void close() throws IOException
    {
        if (mClosed) {
            throw new IOException("Encrypted output stream is closed");
        }
        super.out.close();
        mClosed = true;
        mCipher = null;

    }


    @Override
    public void flush() throws IOException {
        if (mClosed) {
            throw new IOException("Encrypted output stream is closed");
        }
        if (mBuffer.position()  > 0) {
            encryptBufferAndSendData();
        }
    }

    private void encrypt( int pByte ) throws IOException {
        mBuffer.put( (byte) (pByte & 0xff));
    }

    private void encrypt(byte[] pBuffer, int pOffset, int pLength) throws IOException {
       mBuffer.put( pBuffer, pOffset, pLength );
    }

    private void encryptBufferAndSendData() throws IOException
    {
        int tOutSize = 0;
        ByteBuffer tOutBuffer = ByteBuffer.allocate(mBuffer.position() + mCipher.getBlockSize());
        mBuffer.flip();
        try {
            tOutSize = mCipher.doFinal(mBuffer.getByteBuffer(), tOutBuffer);
        }
        catch( GeneralSecurityException e) {
            throw new IOException(e);
        }
        tOutBuffer.flip();

        // Write length Field
        mOutStream.writeInt(tOutSize);

        // Write encrypted payload
        mOutStream.write( tOutBuffer.array(), 0, tOutSize);
        mOutStream.flush();


        mBuffer.clear();
        if (mBuffer.capacity() > 4096) {
            mBuffer = new DynamicByteBuffer( 4096 );
        }
    }

}
