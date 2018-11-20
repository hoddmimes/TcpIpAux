package com.hoddmimes.tcpip.test;

import com.hoddmimes.tcpip.crypto.AESEngine;
import com.hoddmimes.tcpip.crypto.CBCBlockCipher;
import com.hoddmimes.tcpip.crypto.DecryptInputStream;
import com.hoddmimes.tcpip.crypto.EncryptOutputStream;
import org.junit.Test;
import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Random;

public class TestEncryption
{
    byte[]  mSecretSharedKey;


    public TestEncryption() {
    }

    private Throwable initCrypto(boolean pEncrypt, byte[] pSecretKey) {
        byte[] iv;

        try {
            PipedInputStream tInStream = new PipedInputStream();
            PipedOutputStream tOutStream = new PipedOutputStream(tInStream);

            EncryptOutputStream tEncrypt;
            DecryptInputStream tDecrypt;


            if (pEncrypt) {
                tEncrypt = new EncryptOutputStream(tOutStream, pSecretKey);
            } else {
                tDecrypt = new DecryptInputStream(tInStream, pSecretKey);
            }

        } catch (Throwable e) {
            return e;
        }
        return null;
    }

    private Object instanceOf( Class pClass ) {
        try {
            return pClass.newInstance();
        }
        catch(Exception e) {
            return null;
        }
    }

    @Test
    public void testInitialization() {
        Random r = new Random();
        byte[] tKey;

        tKey = new byte[(256/8)];
        r.nextBytes( tKey );
        Assert.assertEquals(null, initCrypto(true, tKey));

        Assert.assertEquals(null, initCrypto(false, tKey));

        tKey = new byte[(128/8)];
        r.nextBytes( tKey );
        Assert.assertEquals(initCrypto(true, tKey).getClass(), InvalidParameterException.class);

        Assert.assertEquals(initCrypto(false, tKey).getClass(), InvalidParameterException.class);
    }

    private boolean compareByteArrays( byte[] a , byte[] b) {
        if (a == null && b == null) {
            return true;
        }
        if (a.length != b.length) {
            return false;
        }

        for( int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }


    public boolean encryptTestBufferSteps() throws Exception {
        Random r = new Random();
        byte[] tKey;

        tKey = new byte[(256/8)];
        r.nextBytes( tKey );

        PipedInputStream  tInStream = new PipedInputStream( 8192 );
        PipedOutputStream tOutStream = new PipedOutputStream( tInStream );



        EncryptOutputStream tEncrypt = new EncryptOutputStream( tOutStream, tKey );
        DecryptInputStream tDecrypt = new DecryptInputStream( tInStream, tKey );

        byte[] xBuffer, yBuffer;
        ByteBuffer cBuffer;

        for( int i = 1; i < 1000; i++) {
            int tSize = i;
            xBuffer = new byte[tSize];
            yBuffer = new byte[tSize];
            r.nextBytes( xBuffer );
            tEncrypt.write( xBuffer );
            tEncrypt.flush();
            tDecrypt.readFully( yBuffer );

            if (!compareByteArrays(xBuffer,yBuffer)) {
                return false;
            }
        }
        return true;
    }

    public boolean encryptTestRandom() throws Exception {
        Random r = new Random();
        byte[] tKey, xBuffer, yBuffer;

        tKey = new byte[(256/8)];
        r.nextBytes( tKey );

        PipedInputStream  tInStream = new PipedInputStream( (1024 * 20) );
        PipedOutputStream tOutStream = new PipedOutputStream( tInStream );

        EncryptOutputStream tEncrypt = new EncryptOutputStream( tOutStream, tKey );
        DecryptInputStream tDecrypt = new DecryptInputStream( tInStream, tKey );



        for( int i = 0; i < 10000; i++) {
            int tSize = 1 + Math.abs(r.nextInt( (1024 * 16)));
            xBuffer = new byte[tSize];
            yBuffer = new byte[tSize];

            r.nextBytes( xBuffer );
            tEncrypt.write( xBuffer );
            tEncrypt.flush();
            tDecrypt.readFully( yBuffer );
            if (!compareByteArrays(xBuffer,yBuffer)) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void testEncryption() throws Exception {
       Assert.assertEquals( true, encryptTestBufferSteps() );
       Assert.assertEquals( true, encryptTestRandom() );
    }


}
