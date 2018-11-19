package com.hoddmimes.tcpip.test;

import com.hoddmimes.tcpip.crypto.Crypto;
import org.junit.Test;
import org.junit.Assert;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Random;

public class TestEncryption
{
    byte[]  mSecretSharedKey;


    public TestEncryption() {
    }

    private Throwable initCrypto( boolean pEncrypt, byte[] pSecretKey ) {
        try {
            Crypto c = new Crypto( pEncrypt, pSecretKey );
        }
        catch( Throwable e) {
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

        tKey = new byte[(128/8)];
        r.nextBytes( tKey );
        Assert.assertEquals(initCrypto(true, tKey).getClass(), InvalidParameterException.class);

        Assert.assertEquals(initCrypto(true, null).getClass(), InvalidParameterException.class);
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

        Crypto tEncrypt = new Crypto( true, tKey );
        Crypto tDecrypt = new Crypto( false, tKey );

        byte[] xBuffer, yBuffer;
        ByteBuffer cBuffer;

        for( int i = 1; i < 1000; i++) {
            int tSize = i;
            xBuffer = new byte[tSize];
            cBuffer = ByteBuffer.allocate( tSize + Integer.BYTES + tEncrypt.getBlockSize());
            r.nextBytes( xBuffer );
            tEncrypt.encrypt( xBuffer,0, cBuffer, tSize );
            byte[] nba = new byte[ cBuffer.position()];
            cBuffer.flip();
            cBuffer.get( nba );
            yBuffer = tDecrypt.decrypt( nba );
            if (!compareByteArrays(xBuffer,yBuffer)) {
                return false;
            }
        }
        return true;
    }

    public boolean encryptTestRandom() throws Exception {
        Random r = new Random();
        byte[] tKey;

        tKey = new byte[(256/8)];
        r.nextBytes( tKey );

        Crypto tEncrypt = new Crypto( true, tKey );
        Crypto tDecrypt = new Crypto( false, tKey );

        byte[] xBuffer, yBuffer;
        ByteBuffer cBuffer;

        for( int i = 0; i < 10000; i++) {
            int tSize = 1 + Math.abs(r.nextInt( 10000));
            xBuffer = new byte[tSize];
            cBuffer = ByteBuffer.allocate( tSize + Integer.BYTES + tEncrypt.getBlockSize());
            r.nextBytes( xBuffer );
            tEncrypt.encrypt( xBuffer,0, cBuffer, tSize );
            byte[] nba = new byte[ cBuffer.position()];
            cBuffer.flip();
            cBuffer.get( nba );
            yBuffer = tDecrypt.decrypt( nba );
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
