package com.hoddmimes.tcpip.test;

import com.hoddmimes.tcpip.crypto.DH;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

public class TestDH {

    @Test
    public void testDHDefault() throws GeneralSecurityException {
        BigInteger P = DH.DEFAULT_PRIME;
        BigInteger G = DH.DEFAULT_G;
        DH tClientDH = new DH(G, P);
        DH tServerDH = new DH(G, P);

        tClientDH.calculateSharedKey(tServerDH.getPublicKey());
        tServerDH.calculateSharedKey(tClientDH.getPublicKey());

        SecretKeySpec csk = tClientDH.getSharedSecretKey();
        SecretKeySpec ssk = tServerDH.getSharedSecretKey();

        Assertions.assertArrayEquals(csk.getEncoded(), ssk.getEncoded());
        //System.out.println("testDHDefault: " + Hex.toHexString(csk.getEncoded()));
        //System.out.println("testDHDefault key size: " + (csk.getEncoded().length * 8) + " bits");
    }
}
