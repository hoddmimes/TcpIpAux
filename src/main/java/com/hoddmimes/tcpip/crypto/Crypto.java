package com.hoddmimes.tcpip.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Random;

public class Crypto
{
    private CBCBlockCipher mCipher;
    private byte[] mInBlkBuf,mOutBlkBuf;
    private boolean mEncrypt;
    private Random mRandom;

    public Crypto(boolean pEncrypt, byte[] pSharedSecretKey ) {
        // Key must be 256 bits and must nut be null
        if ((pSharedSecretKey == null) || ((pSharedSecretKey.length * Byte.SIZE) != 256)) {
            throw new InvalidParameterException("tcpIpAux: Key must be 256 bit");
        }

        mEncrypt = pEncrypt;
        mCipher = new CBCBlockCipher( new AESEngine());
        mCipher.init( pEncrypt, getIV( pSharedSecretKey), pSharedSecretKey);
        mInBlkBuf = new byte[ mCipher.getBlockSize() ];
        mOutBlkBuf = new byte[ mCipher.getBlockSize() ];
        mRandom = new Random( System.nanoTime());
        mRandom.nextBytes( mOutBlkBuf );
    }

    public int getBlockSize() {
        return mCipher.getBlockSize();
    }

    private  byte[] getIV( byte[] pSSK ) {
        BigInteger k = new BigInteger( pSSK );

        DH.MyRandom tRnd = new DH.MyRandom(k.longValue());
        byte[] iv = new byte[ mCipher.getBlockSize() ];
        tRnd.nextBytes( iv );
        return iv;
    }



    public void encrypt(byte[] pInBuffer, int pInOffset, ByteBuffer pOutBuffer, int pLength) throws IOException {
        int tPos = 0;

        /**
         * Encrypted messages being sent over the wire will be an multiple of the cipher blocksize and
         * have a preceeding length field infront ov the userdata i.e. the length field will be part of the encrypted data
         * Therefor there is some special process of the first block depending on if encryp/decrypt is done with the purpose to
         * set / find the true user data length.
         **/

        // write the true user data length in the beginning of the encrypted data
        NumConvert.int2Buffer(pLength, mInBlkBuf, 0);
        // Fill up the buffer or move what is left of the message
        int tMovSize = Math.min(pLength, (mInBlkBuf.length - Integer.BYTES));
        // Move some user data to the inblock
        System.arraycopy(pInBuffer, pInOffset + 0, mInBlkBuf, Integer.BYTES, tMovSize);
        // Encrypt data
        mCipher.processBlock(mInBlkBuf, 0, mOutBlkBuf, 0);
        // Move data to out buffer
        pOutBuffer.put(mOutBlkBuf);
        // Update postion pointer with data length being moved
        tPos += tMovSize;

        // Move the rest of the user data
        while (tPos < pLength) {
            if ((pLength - tPos) >= mCipher.getBlockSize()) {
                mCipher.processBlock(pInBuffer, pInOffset + tPos, mOutBlkBuf, 0);

            } else {
                System.arraycopy(pInBuffer, pInOffset + tPos, mInBlkBuf, 0, (pLength - tPos));
                mCipher.processBlock(mInBlkBuf, 0, mOutBlkBuf, 0);
            }
            pOutBuffer.put(mOutBlkBuf);
            tPos += mCipher.getBlockSize();
        }
    }


    public  byte[] decrypt(byte[] pInBuffer ) throws IOException
    {
        if ((pInBuffer.length % mCipher.getBlockSize()) != 0) {
            throw new InvalidParameterException("tcpIpAux: Encrypted network message must be a multiple of the cipher block size ");
        }

        /**
         * Encrypted messages being sent over the wire must be an multiple of the cipher blocksize and
         * have a preceeding length field in front of the userdata i.e. the length field will be part of the encrypted data
         * Therefor there is some special process of the first block depending on if encryp/decrypt is done with the purpose to
         * set / find the true user data length.
         **/

        mCipher.processBlock(pInBuffer, 0 , mOutBlkBuf, 0); // Decrypt the first block
        int tUserDataLength = NumConvert.buffer2Int( mOutBlkBuf, 0); // Read user data length
        byte[] tUserData = new byte[ tUserDataLength ];
        int tMovedDataLength = Math.min( tUserDataLength, (mCipher.getBlockSize() - Integer.BYTES));
        System.arraycopy( mOutBlkBuf, Integer.BYTES, tUserData, 0, tMovedDataLength );

        int tInDataOffet = Integer.BYTES + tMovedDataLength;




        while (tMovedDataLength < tUserDataLength) {
            mCipher.processBlock(pInBuffer, tInDataOffet, mOutBlkBuf, 0);
            if ((tUserDataLength - tMovedDataLength) >= mCipher.getBlockSize()) {
                System.arraycopy( mOutBlkBuf, 0, tUserData, tMovedDataLength, mOutBlkBuf.length );
            } else {
                System.arraycopy( mOutBlkBuf, 0, tUserData, tMovedDataLength, (tUserDataLength - tMovedDataLength) );
            }
            tMovedDataLength += mCipher.getBlockSize();
            tInDataOffet += mCipher.getBlockSize();
        }
        return tUserData;
    }
}
