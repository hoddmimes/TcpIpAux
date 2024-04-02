package com.hoddmimes.tcpip.test;

import com.hoddmimes.tcpip.TcpIpConnectionTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class TestNetwork
{


    @Test
    public void test1PlainConnection() {
        Assertions.assertEquals(null, runPlainConnection(false));
    }

    @Test
    public void test2EncryptConnection() {
        Assertions.assertEquals(null, runEncryptConnection(false));
    }

    @Test
    public void test3CompressionConnection() {
        Assertions.assertEquals(null, runCompressedConnection(false));
    }

    @Test
    public void test4CompressionAndEncryptionConnection() {
        Assertions.assertEquals(null, runCompressedAndCryptoConnection(false));
    }

    @Test
    public void test5SigninConnection() {
        Assertions.assertEquals(null, runSigninConnection(false));
    }

    private Object runSigninConnection( boolean pTrace )
    {
        NetworkServer tServer = new NetworkServer( 0);
        tServer.startServer(pTrace);
        try { Thread.sleep(1000L); }
        catch( InterruptedException e) {}

        NetworkClient tClient = new NetworkClient();
        Object tResult = tClient.test( TcpIpConnectionTypes.PLAIN+ TcpIpConnectionTypes.SSH_SIGNING,
                "id_rsa_with_password_foobar","foobar",
                10000,
                1,
                5000,
                ('Z' - 'A') ,
                pTrace );
        tServer.close();
        return tResult;
    }

    private Object runPlainConnection( boolean pTrace )
    {
        NetworkServer tServer = new NetworkServer( 0);
        tServer.startServer(pTrace);
        try { Thread.sleep(1000L); }
        catch( InterruptedException e) {}

        NetworkClient tClient = new NetworkClient();
        Object tResult = tClient.test( TcpIpConnectionTypes.PLAIN, pTrace );
        tServer.close();
        return tResult;
    }

    private Object runCompressedConnection(boolean pTrace)
    {
        NetworkServer tServer = new NetworkServer( 0 );
        tServer.startServer(pTrace);
        try { Thread.sleep(1000L); }
        catch( InterruptedException e) {}

        NetworkClient tClient = new NetworkClient();
        Object tResult = tClient.test( TcpIpConnectionTypes.COMPRESS, 10000, 1000, 5000, ('H' - 'A') , pTrace );
        tServer.close();
        return tResult;
    }

    private Object runCompressedAndCryptoConnection(boolean pTrace) {
        NetworkServer tServer = new NetworkServer(0);
        tServer.startServer(pTrace);
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
        }

        NetworkClient tClient = new NetworkClient();
        Object tResult = tClient.test(TcpIpConnectionTypes.COMPRESS + TcpIpConnectionTypes.ENCRYPT, 10000, 500, 3000, ('H' - 'A'), pTrace);
        tServer.close();
        return tResult;
    }

    private Object runEncryptConnection(boolean pTrace) {
        NetworkServer tServer = new NetworkServer(0);
        tServer.startServer(pTrace);
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
        }

        NetworkClient tClient = new NetworkClient();
        Object tResult = tClient.test(TcpIpConnectionTypes.ENCRYPT, pTrace);
        tServer.close();
        return tResult;
    }
}
