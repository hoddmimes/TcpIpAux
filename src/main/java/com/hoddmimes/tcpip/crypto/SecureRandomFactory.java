package com.hoddmimes.tcpip.crypto;

import java.security.SecureRandom;
import java.util.Random;

public class SecureRandomFactory {

    protected static SecureRandom getInstance() {
        // Initialize the secure random
        Random tRandom = new Random(System.nanoTime());
        int tSize = Math.abs(tRandom.nextInt()) % 13373792;
        byte[] tSpace = new byte[tSize];

        long tSleep = Math.abs(tRandom.nextInt()) % 137;
        try {
            Thread.sleep(tSleep);
        } catch (InterruptedException e) {
        }

        long tFreeMem = Runtime.getRuntime().freeMemory();
        long tTime = System.nanoTime();
        long tConstant = 7297352420393016796L;

        long tSeed = tFreeMem ^ tTime ^ tConstant;
        SecureRandom tSecRnd = new SecureRandom(longToBytes(tSeed));
        tSpace = null;
        return tSecRnd;
    }

    private static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }
}
