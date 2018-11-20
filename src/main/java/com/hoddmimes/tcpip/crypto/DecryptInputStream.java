package com.hoddmimes.tcpip.crypto;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

public class DecryptInputStream extends FilterInputStream {
    private int BLOCK_SIZE = 16;
    private int WORKING_BUFFER_SIZE = 64;
    private byte[] mInBlock,mOutBlock;

    private PipedInputStream            mInDecryptStream;
    private PipedOutputStream           mOutDecryptStream;
    private CBCBlockCipher              mCipher;


    /**
     * Creates a <code>FilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param pTcpIpSocketInputStream the underlying input stream, or <code>null</code> if
     *           this instance is to be created without an underlying stream.
     */
    public  DecryptInputStream(InputStream pTcpIpSocketInputStream, byte[] pSharedSecretKey) {
        super(pTcpIpSocketInputStream);
        mInBlock = new byte[ WORKING_BUFFER_SIZE ];
        mOutBlock = new byte[ WORKING_BUFFER_SIZE ];

        try {
            mOutDecryptStream = new PipedOutputStream();
            mInDecryptStream = new PipedInputStream( mOutDecryptStream,4096);

        }
        catch( IOException e) {
            e.printStackTrace();
        }
        mCipher = new CBCBlockCipher( new AESEngine());
        mCipher.init( false, getIV( pSharedSecretKey), pSharedSecretKey);

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

    private void readAndDecryptData() throws IOException
    {
        int tBuffersToRead = 0;
        int tPaddingLength = 0;
        int tOffset = 0;

        /**
         * Two counterparties exchanging encrypted information will commit to the following "protocol"
         * Data exchanged sent/received are always being a multiple of the cipher block size, currently assumed to be
         * 16 bytes.
         * Since data exchanged is not always a multiple of the block size, padding could be used to fill out a block.
         * Ino order to identify padded data a sort of protocoll is to be used.
         *
         * When sending data 1-4 (16 bytes) blocks could be sent in one network write
         * The first byte in the first block will tell how many blocks being sent and how much padding is present in the last block
         * the 3 high bits are used to tell number of network blocks, the 5 lower bits tells the padding size in the last blocks
         *
         */

        // Read first block
        super.read( mInBlock, 0, BLOCK_SIZE );
        mCipher.processBlock( mInBlock, 0, mOutBlock, 0);

        tBuffersToRead = getNetworkBuffers(mOutBlock);
        tPaddingLength = getPaddingLength(mOutBlock);

        tOffset = BLOCK_SIZE;
        // Read remaining blocks (if any).
        for( int i = 1; i < tBuffersToRead; i++ ) {
            super.read( mInBlock, tOffset, BLOCK_SIZE );
            mCipher.processBlock( mInBlock, tOffset, mOutBlock, tOffset);
            tOffset += BLOCK_SIZE;
        }

        // The user data length that should be passed on is read data minus
        // intial one byte used for telling number of network buffer and padding size
        // and the number of padding bytes
        int tMoveSize = (tBuffersToRead * BLOCK_SIZE) - 1 - tPaddingLength;

        // Ignore the first byte. It is the overhead byte telling network buffer and padding length
        // Write the buffer to the uncrypt output stream
        mOutDecryptStream.write( mOutBlock, 1, tMoveSize);
        mOutDecryptStream.flush();
    }

    int decrypt( byte[] pBuffer, int pOffset, int pLength ) throws IOException
    {
        if (mInDecryptStream.available() == 0) {
            readAndDecryptData();
        }
        return mInDecryptStream.read( pBuffer, pOffset, pLength);
    }



    int decrypt() throws IOException {
        if (mInDecryptStream.available() == 0) {
            readAndDecryptData();
        }
        return mInDecryptStream.read();
    }


    public void readFully( byte[] pBuffer ) throws IOException
    {
        readFully(pBuffer,0, pBuffer.length);
    }

    public void readFully( byte[] pBuffer, int pOffset, int pLength  ) throws IOException
    {
        int n = 0;
        while( n < pLength ) {
            n += read( pBuffer, n, (pLength - n ));
        }
    }

    @Override
    public int  read( byte[] pBuffer ) throws IOException {
        return decrypt(pBuffer, 0, pBuffer.length);
    }


    @Override
    public int read( byte[] pBuffer, int pOffset, int pLength  ) throws IOException
    {
        return decrypt(pBuffer, pOffset, pLength);
    }

    @Override
    public int read() throws IOException
    {
        return decrypt();
    }

    int getPaddingLength( byte[] pBuffer ) {
        byte pValue = pBuffer[0];
        return  (pValue & 0x1f); //(0-31)
    }


    int getNetworkBuffers( byte[] pBuffer) {
        byte pValue = pBuffer[0];
        return  ((pValue >> 5) & 0x7); //(0-7)
    }
}
