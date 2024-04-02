package com.hoddmimes.tcpip.crypto;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/*

This class will decode Open SSH public key files to BounceCastle PublicKey objects.
SSH is a common and very popular mechanism to access remote hosts, especially in Linux/Unix environments.
OpenSSH however tend to have their own implementation of the standard, when it comes to key format.

In the Bouncecastle utility there no support for parsing OpenSSH public keyfiles. This class will bridge the gap
of parsing OpenSSH public key file and make the public keys available in Bouncecastle.

The class supports RSA,EC and DSA keys

When it comes to private keys Bouncecastle have the ability to parse OpenSSH private keys given that they
been generated as PEM file e.g

$ ssh-keygen -t rsa -b 2048 -m PEM

 */


public class OpenSSHPublicKeyDecoder {
    String mKeyType;
    ByteBuffer mDecodedKeyBuffer;

    public OpenSSHPublicKeyDecoder()
    {
        Security.addProvider( new BouncyCastleProvider());
    }

    public PublicKey decodePublicKey(File pPublicKeyFile) throws GeneralSecurityException, IOException
    {
        if (!pPublicKeyFile.exists()) {
            throw new GeneralSecurityException("Can not find public SSH key file \"" + pPublicKeyFile +"\"");
        }
        if (!pPublicKeyFile.canRead()) {
            throw new GeneralSecurityException("Can read public SSH key file \"" + pPublicKeyFile +"\"");
        }
        String tKeyFileLine = new String(Files.readAllBytes( pPublicKeyFile.toPath()),StandardCharsets.UTF_8);
        return decodePublicKey( tKeyFileLine);
    }

    public PublicKey decodePublicKey(String pKeyLineString) throws GeneralSecurityException {
        String[] tKeyElement = pKeyLineString.split(" ");
        if (tKeyElement.length < 2) {
            throw new IllegalArgumentException("invalid key format");
        }
        if (!tKeyElement[1].startsWith("AAAA")) {
            throw new IllegalArgumentException("no Base64 part to decode");
        }
        mKeyType = tKeyElement[0];
        byte[] tDecodedKeyBytes = Base64.getDecoder().decode(tKeyElement[1]);
        mDecodedKeyBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(tKeyElement[1]));

        if (mKeyType.contentEquals("ssh-rsa")) {
            String tKeyIdType = decodeType();
            BigInteger e = decodeBigInt();
            BigInteger m = decodeBigInt();
            RSAPublicKeySpec spec = new RSAPublicKeySpec(m, e);
            return KeyFactory.getInstance("RSA", "BC").generatePublic(spec);
        } else if (mKeyType.equals("ssh-dss")) {
            String tKeyIdType = decodeType();
            BigInteger p = decodeBigInt();
            BigInteger q = decodeBigInt();
            BigInteger g = decodeBigInt();
            BigInteger y = decodeBigInt();
            DSAPublicKeySpec tSpec = new DSAPublicKeySpec(y, p, q, g);
            return KeyFactory.getInstance("DSA","BC").generatePublic(tSpec);

        } else if (mKeyType.startsWith("ecdsa-sha2-") &&
                (mKeyType.endsWith("nistp256") || mKeyType.endsWith("nistp384") || mKeyType.endsWith("nistp521"))) {

            String tFullKeyTypeId = decodeType();
            String tKeyTypeId = decodeType();

            byte[] qParam = decodeQParameter();
            PublicKey tPubKey =  getPublicECKey(tKeyTypeId, qParam);

            /*
            int tCoordinateLen = (mDecodedKeyBuffer.getInt() - 1) / 2; // Total coordinate-field length, minus one byte compress-flag dived by 2 /x,y coordinate)
            byte tCompressFlag = mDecodedKeyBuffer.get();   // Get compress flag
            if (tCompressFlag != 0x04) {
                 throw new GeneralSecurityException("Invalid EC point coordination compression flag (we can only deal with uncompressed format)");
            }
            BigInteger  x_cord = new BigInteger(decodeCoordinate(tCoordinateLen));
            BigInteger  y_cord = new BigInteger(decodeCoordinate(tCoordinateLen));
            System.out.println("x: " + x_cord.toString(16));
            System.out.println("y: " + y_cord.toString(16));
            PublicKey tPubKey =  getPublicECKey(tKeyTypeId, x_cord, y_cord);
            */

            return tPubKey;
        } else {
            throw new NoSuchAlgorithmException("Unknown public SSH key type");
        }
    }



    private ECPublicKey getPublicECKey(String pKeyTypeId, BigInteger x, BigInteger y) throws GeneralSecurityException {

        try {
            String name = pKeyTypeId.replace("nist", "sec") + "r1";
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");

            ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(name);
            ECPoint tPoint = ecSpec.getCurve().createPoint(x,y);

            ECPublicKeySpec pubSpec = new ECPublicKeySpec(tPoint, ecSpec);
            ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(pubSpec);
            return publicKey;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private ECPublicKey getPublicECKey(String pKeyTypeId, byte[] qParam) throws GeneralSecurityException {

        try {
            String name = pKeyTypeId.replace("nist", "sec") + "r1";
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");

            ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(name);


            ECPoint point = ecSpec.getCurve().decodePoint(qParam);
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);
            ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(pubSpec);
            return publicKey;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    byte[] decodeQParameter() {
        int tLen = mDecodedKeyBuffer.getInt();
        byte[] tBuffer = new byte[ tLen ];
        mDecodedKeyBuffer.get( tBuffer );
        return tBuffer;
    }

    private byte[] decodeCoordinate( int pCoordinateLength){
        byte[] p = new byte[pCoordinateLength];
        for (int i = 0; i < pCoordinateLength; i++) {
          p[i] = mDecodedKeyBuffer.get();
        }
        return p;
    }

    private String decodeType() {
        int tLength = mDecodedKeyBuffer.getInt();
        String tType = new String(mDecodedKeyBuffer.array(), mDecodedKeyBuffer.position(), tLength, StandardCharsets.UTF_8);
        mDecodedKeyBuffer.position(mDecodedKeyBuffer.position() + tLength);
        return tType;
    }


    private BigInteger decodeBigInt() {
        int tLength = mDecodedKeyBuffer.getInt();
        byte[] bigIntBytes = new byte[tLength];
        mDecodedKeyBuffer.get(bigIntBytes);
        return new BigInteger(bigIntBytes);
    }

    public static void main(String[] args) {
       try {
           Security.addProvider(new BouncyCastleProvider());
           OpenSSHPublicKeyDecoder tDecoder = new OpenSSHPublicKeyDecoder();

           File tKeyFile = new File("id_rsa.pub");
           String tKeyLineString = new String(Files.readAllBytes(tKeyFile.toPath()), StandardCharsets.UTF_8 );
           PublicKey tPubKey = tDecoder.decodePublicKey(tKeyLineString);
           System.out.println(tPubKey.getAlgorithm());


           tKeyFile = new File("id_dsa.pub");
           tKeyLineString = new String(Files.readAllBytes(tKeyFile.toPath()), StandardCharsets.UTF_8 );
           tPubKey = tDecoder.decodePublicKey(tKeyLineString);
           System.out.println(tPubKey.getAlgorithm());

           tKeyFile = new File("id_ecdsa.pub");
           tKeyLineString = new String(Files.readAllBytes(tKeyFile.toPath()), StandardCharsets.UTF_8 );
           tPubKey = tDecoder.decodePublicKey(tKeyLineString);
           System.out.println(tPubKey.getAlgorithm());
       }
       catch( Exception e ) {
         e.printStackTrace();
       }
    }
}
