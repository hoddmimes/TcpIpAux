package com.hoddmimes.tcpip.test;

import com.hoddmimes.tcpip.TcpIpConnectionTypes;

import org.junit.Assert;
import org.junit.Test;

public class TestNetwork
{

    @Test
    public void testPlainConnection() {
      Assert.assertEquals(null, runPlainConnection(false));
    }

    @Test
    public void testEcryptConnection() {
      Assert.assertEquals(null, runEncryptConnection(false));
    }

    @Test
    public void testCompressionConnection() {
        Assert.assertEquals(null, runCompressedConnection(false));
    }

    @Test
    public void testCompressionAndEncryptionConnection() {
      Assert.assertEquals(null, runCompressedAndCryptoConnection(false));
    }

    private Object runPlainConnection( boolean pTrace )
    {
        NetworkServer tServer = new NetworkServer( null);
        tServer.startServer(pTrace);
        try { Thread.sleep(1000L); }
        catch( InterruptedException e) {}

        NetworkClient tClient = new NetworkClient();
        Object tResult = tClient.test( TcpIpConnectionTypes.Plain, pTrace );
        tServer.close();
        return tResult;
    }

    private Object runCompressedConnection(boolean pTrace)
    {
        NetworkServer tServer = new NetworkServer( null );
        tServer.startServer(pTrace);
        try { Thread.sleep(1000L); }
        catch( InterruptedException e) {}

        NetworkClient tClient = new NetworkClient();
        Object tResult = tClient.test( TcpIpConnectionTypes.Compression, pTrace );
        tServer.close();
        return tResult;
    }

    private Object runCompressedAndCryptoConnection(boolean pTrace) {
        NetworkServer tServer = new NetworkServer(null);
        tServer.startServer(pTrace);
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
        }

        NetworkClient tClient = new NetworkClient();
        Object tResult = tClient.test(TcpIpConnectionTypes.Compression_Encrypt, pTrace);
        tServer.close();
        return tResult;
    }

    private Object runEncryptConnection(boolean pTrace) {
        NetworkServer tServer = new NetworkServer(null);
        tServer.startServer(pTrace);
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
        }

        NetworkClient tClient = new NetworkClient();
        Object tResult = tClient.test(TcpIpConnectionTypes.Encrypt, pTrace);
        tServer.close();
        return tResult;
    }
}
