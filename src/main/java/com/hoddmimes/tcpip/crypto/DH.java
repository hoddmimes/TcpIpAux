package com.hoddmimes.tcpip.crypto;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.digest.SHA3;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;


public class DH {
    private static final int SECRET_KEY_BYTE_LENGTH = 384;
    public static final BigInteger DEFAULT_PRIME = new BigInteger("b25c9fccfb28e3cbb14f34aa55194d066e22ed257e18ea2368f40b82559f7590b5e9b5ec819b3fb8358c9cadd09d88b33" +
            "729b4935dfe4692c91db26faa896193c4eb5069cebb6857bc97ad98dbfb1f6f2c287d34e02d3923df32cc2e9035c8a09af280943b637a8402beb846d8d3f69c343066a1fe164e0d5c1bfb5f548e5a" +
            "6ee50ce917eb1df5de4f105a7473fcaac056cb7e3527b971ce6573540a7545b8dda7ad020f31dc3e25d1afa7289ae92fefc35aa650e6bbc18775037863e44df071b0699a85c11d39a772f42c768a6" +
            "4b184bd571ea1247f7d3c7e38e5e3705a76ca1dbfd37d717d9a581e4a49ae1446855f33058d7bad6b760808abe3cb8097f0243", 16);

    public static final BigInteger DEFAULT_G = new BigInteger("3");

    private final BigInteger mPrime;          // P
    private final BigInteger mCommonBase;     // G
    private final BigInteger mMyPublicKey;
    private final BigInteger mMySecretKey;
    private byte[] mSecretKey;


    public DH(BigInteger pCommonBase) {
        this( pCommonBase, DEFAULT_PRIME );
    }

    public DH(BigInteger pCommonBase, BigInteger pPrime) {
        mSecretKey = null;
        SecureRandom tSecureRandom = new SecureRandom();
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

        try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] tSalt = md.digest(tSharedKey.toByteArray());
        PKCS5S2ParametersGenerator tGen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        tGen.init( tSharedKey.toByteArray(), tSalt, 4096);
        mSecretKey =  ((KeyParameter) tGen.generateDerivedParameters(256)).getKey();
        }
        catch( Exception e) {
            throw new RuntimeException(e);
        }
        return mSecretKey;
    }




    public BigInteger getG() {
        return new BigInteger("3");
    }

    public BigInteger getP() {
        return mPrime;
    }

    public SecretKeySpec getSharedSecretKey() throws NoSuchAlgorithmException {
        return new SecretKeySpec(mSecretKey,"AES");
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
