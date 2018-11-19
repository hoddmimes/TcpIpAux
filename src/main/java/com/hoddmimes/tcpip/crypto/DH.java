package com.hoddmimes.tcpip.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;


public class DH {
    private static int SECRET_KEY_BYTE_LENGTH = 384;
    public static final BigInteger DEFAULT_PRIME = new BigInteger("b25c9fccfb28e3cbb14f34aa55194d066e22ed257e18ea2368f40b82559f7590b5e9b5ec819b3fb8358c9cadd09d88b33" +
            "729b4935dfe4692c91db26faa896193c4eb5069cebb6857bc97ad98dbfb1f6f2c287d34e02d3923df32cc2e9035c8a09af280943b637a8402beb846d8d3f69c343066a1fe164e0d5c1bfb5f548e5a" +
            "6ee50ce917eb1df5de4f105a7473fcaac056cb7e3527b971ce6573540a7545b8dda7ad020f31dc3e25d1afa7289ae92fefc35aa650e6bbc18775037863e44df071b0699a85c11d39a772f42c768a6" +
            "4b184bd571ea1247f7d3c7e38e5e3705a76ca1dbfd37d717d9a581e4a49ae1446855f33058d7bad6b760808abe3cb8097f0243", 16);

    public static final BigInteger DEFAULT_G = new BigInteger("3");

    private BigInteger mPrime;          // P
    private BigInteger mCommonBase;     // G
    private BigInteger mMyPublicKey;
    private BigInteger mMySecretKey;
    private byte[] mSecretKey;


    public DH(BigInteger pCommonBase) {
        this( pCommonBase, DEFAULT_PRIME );
    }

    public DH(BigInteger pCommonBase, BigInteger pPrime) {
        mSecretKey = null;
        SecureRandom tSecureRandom = SecureRandomFactory.getInstance();
        mCommonBase = pCommonBase;
        mPrime = pPrime;

        byte[] tKey = new byte[SECRET_KEY_BYTE_LENGTH];
        tSecureRandom.nextBytes(tKey);
        mMySecretKey = new BigInteger(tKey);
        //System.out.println("my secret length: " + mMySecretKey.toString(16).length() + " key: " + mMySecretKey.toString(16));

        mMyPublicKey = mCommonBase.modPow(mMySecretKey, mPrime);

    }

    public BigInteger getPublicKey() {
        return mMyPublicKey;
    }

    public byte[] calculateSharedKey(BigInteger pClientPublicKey) {
        BigInteger tSharedKey = pClientPublicKey.modPow(mMySecretKey, mPrime);
        //System.out.println("shared-key: " + tSharedKey.toString(16));

        String tKeyStr = tSharedKey.toString(16);
        //System.out.println("Length: " + tKeyStr.length());

        mSecretKey = new byte[32]; // 256 bits

        if (tKeyStr.length() >= 128) {
            int tSegments = tKeyStr.length() / 64;
            List<BigInteger> tBIV = new ArrayList();
            for( int i = 0; i < tSegments; i++)
            {
                tBIV.add( new BigInteger( tKeyStr.substring(i*64, (i+1) * 64), 16));
            }
            BigInteger tKey = tBIV.get(0);
            for( int i = 1; i < tBIV.size(); i++) {
                tKey = tKey.xor( tBIV.get(i));
            }
            tKeyStr = tKey.toString(16);
            //System.out.println(" tKey Length: " + tKeyStr.length());
            //System.out.println("shared-key: " + tKeyStr );


            for( int i = 0; i < 32; i++ ) {
                mSecretKey[i] = (byte) (Integer.parseInt(tKeyStr.substring(i*2, (i+1)*2), 16) & 0xff);
            }
        } else {
            MyRandom tRandom = new MyRandom(tSharedKey.longValue());
            byte[] tSharedKeyBytes = tSharedKey.toByteArray();
            int tSharedKeySize = tSharedKeyBytes.length;
            for (int i = 0; ((i < tSharedKeySize) && (i < mSecretKey.length)); i++) {
                mSecretKey[i] = tSharedKeyBytes[i];
            }

            byte[] tFiller = new byte[mSecretKey.length];
            tRandom.nextBytes(tFiller);

            for (int i = tSharedKeySize; i < mSecretKey.length; i++) {
                mSecretKey[i] = tFiller[i];
            }
        }

        return mSecretKey;
    }





    public BigInteger getG() {
        return new BigInteger("3");
    }



    public BigInteger getP() {
        return mPrime;
    }

    public byte[] getSharedSecretKey() {
        return mSecretKey;
    }

    static class MyRandom {
        long mSeed;

        MyRandom(long pSeed) {
            mSeed = pSeed;
        }


        void nextBytes(byte[] pBytes) {
            int tRandom;
            // Do a little bit unrolling of the above algorithm.
            int max = pBytes.length & ~0x3;
            for (int i = 0; i < max; i += 4) {
                tRandom = next(32);
                pBytes[i] = (byte) tRandom;
                pBytes[i + 1] = (byte) (tRandom >> 8);
                pBytes[i + 2] = (byte) (tRandom >> 16);
                pBytes[i + 3] = (byte) (tRandom >> 24);
            }
            if (max < pBytes.length) {
                tRandom = next(32);
                for (int j = max; j < pBytes.length; j++) {
                    pBytes[j] = (byte) tRandom;
                    tRandom >>= 8;
                }
            }
        }

        private int next(int pBits) {
            mSeed = (mSeed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
            return (int) (mSeed >>> (48 - pBits));
        }
    }


    public static void main( String[] pArgs ) {
        BigInteger P = DH.DEFAULT_PRIME;
        BigInteger G = DH.DEFAULT_G;
        System.out.println("P : " + P.toString(16) + "  G: " + G.toString(16));

        DH tAlice = new DH( G,P );
        DH tBob = new DH( G,P );

        BigInteger ask = new BigInteger(tBob.calculateSharedKey( tAlice.getPublicKey() ));
        BigInteger bsk = new BigInteger(tAlice.calculateSharedKey( tBob.getPublicKey() ));
        System.out.println("Alice  Public: " + tAlice.getPublicKey().toString(16));
        System.out.println("Bob    Public: " + tBob.getPublicKey().toString(16));
        System.out.println("Alice : " + ask.toString(16));
        System.out.println("Bob   : " + bsk.toString(16));
    }
}
