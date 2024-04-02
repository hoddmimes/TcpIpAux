package com.hoddmimes.tcpip.test;

import com.hoddmimes.tcpip.crypto.DecryptInputStream;
import com.hoddmimes.tcpip.crypto.EncryptOutputStream;
import com.hoddmimes.tcpip.impl.CompressionInputStream;
import com.hoddmimes.tcpip.impl.CompressionOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;

public class TestEncryptStream
{
    private static final String CIPHER_ALGO = "AES/CBC/PKCS5Padding";

    public static void main(String[] args) {
        TestEncryptStream t = new TestEncryptStream();
        try {
            t.test();
        }
        catch( Exception e ) { e.printStackTrace(); }
    }

    private SecretKeySpec generateKey(int tSizeBits ) {
        SecureRandom r = new SecureRandom();
        byte[] tKey;

        tKey = new byte[(tSizeBits/8)];
        r.nextBytes( tKey );
        return new SecretKeySpec( tKey,"AES");
    }

    private IvParameterSpec generateIV(SecretKeySpec tKey ) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return new IvParameterSpec(md.digest(tKey.getEncoded()));
        }
        catch( Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void test() throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        SecretKeySpec tSecretKey = generateKey(256);

        // Create transport layer using pipes
        PipedInputStream tInPipeStream = new PipedInputStream( 4096);
        PipedOutputStream tOutPipeStream = new PipedOutputStream( tInPipeStream );

        // Create encryption layer

        Cipher tEncryptCipher = Cipher.getInstance(CIPHER_ALGO, "BC");
        Cipher tDecryptCipher = Cipher.getInstance(CIPHER_ALGO, "BC");

        tEncryptCipher.init( Cipher.ENCRYPT_MODE, tSecretKey, generateIV( tSecretKey ));
        tDecryptCipher.init(Cipher.DECRYPT_MODE, tSecretKey, generateIV( tSecretKey ));

        DecryptInputStream tDecryptStream = new DecryptInputStream( tInPipeStream, tDecryptCipher );
        EncryptOutputStream tEncryptStream = new EncryptOutputStream( tOutPipeStream, tEncryptCipher);

        // create compression layer
        BufferedOutputStream compOutStream =  new BufferedOutputStream(new  CompressionOutputStream(tOutPipeStream));
        BufferedInputStream compInStream = new BufferedInputStream( new  CompressionInputStream(tInPipeStream));



        DataOutputStream tOut = new DataOutputStream( compOutStream );
        DataInputStream tIn = new DataInputStream( compInStream );


        String tDataStr = "buy the ticket take the ride.";
        tOut.writeInt(tDataStr.length());
        tOut.write( tDataStr.getBytes(StandardCharsets.UTF_8), 0, tDataStr.length());
        tOut.flush();

        int tSize = tIn.readInt();
        byte tData[] = new byte[tSize];
        tIn.readFully(tData, 0, tData.length);
        System.out.println( new String( tData, StandardCharsets.UTF_8 ));

         tDataStr = "All my means are sane, my motive and my object mad.";
        tOut.writeInt(tDataStr.length());
        tOut.write( tDataStr.getBytes(StandardCharsets.UTF_8));
        tOut.flush();

         tSize = tIn.readInt();
         tData = new byte[tSize];
        tIn.readFully(tData, 0, tData.length);
        System.out.println( new String( tData, StandardCharsets.UTF_8 ));

    }
}
