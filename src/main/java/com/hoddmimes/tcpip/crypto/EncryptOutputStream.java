package com.hoddmimes.tcpip.crypto;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;


public class EncryptOutputStream extends FilterOutputStream {
    private final int BLOCK_SIZE = 16; // Do not change
    private final int WORKING_BUFFER_SIZE = 64; // Do not change this unless you understand what you are doing
    private ByteBuffer mInBuffer,mOutBuffer;

    private CBCBlockCipher mCipher;
    private OutputStream mTcpIpSocketOutputStream;
    /**
     * Creates an output stream filter built on top of the specified
     * underlying output stream.
     *
     * @param pTcpIpSocketOutputStream the underlying output stream to be assigned to
     *            the field <tt>this.out</tt> for later use, or
     *            <code>null</code> if this instance is to be
     *            created without an underlying stream.
     *
     * @param pSharedSecretKey, common secret (256 bit) key used by client and server.
     */
    public EncryptOutputStream(OutputStream pTcpIpSocketOutputStream, byte[] pSharedSecretKey ) {
        super(pTcpIpSocketOutputStream);
        mInBuffer = ByteBuffer.allocate( WORKING_BUFFER_SIZE );
        mOutBuffer = ByteBuffer.allocate( WORKING_BUFFER_SIZE );
        mInBuffer.position(1); // save byte "0" for padding and nework buffer length which is a multiple of 16
        mTcpIpSocketOutputStream = pTcpIpSocketOutputStream;
        mCipher = new CBCBlockCipher( new AESEngine());
        mCipher.init(true, getIV( pSharedSecretKey), pSharedSecretKey);
        if (BLOCK_SIZE != mCipher.getBlockSize()) {
            throw new RuntimeException("Wow, we have an hard assumption that the cipher block size is 16 bytes, that does not seems to be true");
        }
        if (pSharedSecretKey.length != (256/8)) {
            throw new InvalidParameterException("Common secret key must be 256 bits");
        }
    }

    private  byte[] getIV( byte[] pSSK ) {
        BigInteger k = new BigInteger( pSSK );

        DH.MyRandom tRnd = new DH.MyRandom(k.longValue());
        byte[] iv = new byte[ mCipher.getBlockSize() ];
        tRnd.nextBytes( iv );
        return iv;
    }



    @Override
    public void write( byte[] pBuffer ) throws IOException {
        encrypt( pBuffer, 0, pBuffer.length );
    }

    @Override
    public void write( byte[] pBuffer, int pOffset, int pLength  ) throws IOException
    {
        encrypt( pBuffer, pOffset, pLength );
    }

    @Override
    public void write(  int pByte ) throws IOException
    {
        encrypt( pByte );
    }


    @Override
    public void flush() throws IOException {
        int tPos = mInBuffer.position();
        if (tPos  > 1) {
            encryptBufferAndSendData( mInBuffer, true );
        }
    }

    private void encrypt( int pByte ) throws IOException {
        if (mInBuffer.remaining() == 0) {
            encryptBufferAndSendData( mInBuffer, false );
        }
        mInBuffer.put( (byte) (pByte & 0xff));
    }


    private void encryptBufferAndSendData( ByteBuffer pBuffer, boolean pFlush) throws IOException
    {
        byte[] tOutBlock = new byte[ WORKING_BUFFER_SIZE ];
        int tNetworkBlocks, tPaddingLength;

        if ((pBuffer.position() % BLOCK_SIZE) != 0) {
            tNetworkBlocks = (pBuffer.position() / BLOCK_SIZE) + 1;
            tPaddingLength = (tNetworkBlocks * BLOCK_SIZE) - pBuffer.position();
        } else {
            tNetworkBlocks = (pBuffer.position() / BLOCK_SIZE);
            tPaddingLength = (tNetworkBlocks * BLOCK_SIZE) - pBuffer.position();
        }

        setNetworkBuffersAndPaddingLength(pBuffer, tNetworkBlocks, tPaddingLength);
        for( int i = 0; i < tNetworkBlocks; i++ ) {
            mCipher.processBlock( pBuffer.array(), (i * BLOCK_SIZE), tOutBlock, (i * BLOCK_SIZE) );
        }
        mTcpIpSocketOutputStream.write(tOutBlock, 0, (tNetworkBlocks * BLOCK_SIZE));
        mTcpIpSocketOutputStream.flush();

        // Clear input buffer
        pBuffer.clear();
        pBuffer.position(1); // first byte is dedicated to set number of blocks sent and padding size
    }


    private int encrypt(byte[] pBuffer, int pOffset, int pLength) throws IOException {
        int tPos = 0;
        while(  tPos < pLength ) {
            if (mInBuffer.remaining() == 0) {
                encryptBufferAndSendData( mInBuffer, false );
            } else {
                int tMovSize = Math.min((pLength - tPos), mInBuffer.remaining());
                mInBuffer.put(pBuffer, (pOffset + tPos), tMovSize);
                tPos += tMovSize;
            }
        }
        return tPos;
    }


    void setPaddingLength( ByteBuffer pBuffer, int pPaddingLength ) {
        byte pValue = pBuffer.get(0);
        pValue = (byte) ((pValue & 0xe0) + (pPaddingLength & 0x1f));
        pBuffer.put(0,pValue);
    }

    void setNetworkBuffers( ByteBuffer pBuffer, int pNetworkLength ) {
        byte pValue = pBuffer.get(0);
        /**
         * We are always sending one block, so the network length tells number of additional blocks sent i.e 0-3
         * for that we just need two bits :-) remaining 6 bit (0-63) is jus how much padding being used in the
         * last block sent :-) (sub optimized ?)
         */
        pValue = (byte) (((pNetworkLength & 0x7) << 6) + (pValue & 0x1f));
        pBuffer.put(0,pValue);
    }

    void setNetworkBuffersAndPaddingLength( ByteBuffer pBuffer, int pNetworkLength, int pPaddingLength ) {
        byte pValue = pBuffer.get(0);
        pValue = (byte) ((pNetworkLength << 5) + (pPaddingLength & 0x1f));
        pBuffer.put(0,pValue);
    }

}
