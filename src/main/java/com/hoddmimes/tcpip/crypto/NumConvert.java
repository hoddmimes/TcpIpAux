package com.hoddmimes.tcpip.crypto;

public class NumConvert {

    public static void short2Buffer(short pValue, byte[] pBuffer, int pOffset) {
        pBuffer[pOffset + 0] = (byte) (pValue >>> 8);
        pBuffer[pOffset + 1] = (byte) (pValue >>> 0);
    }

    public static short buffer2Short(byte[] pBuffer, int pOffset) {
        short tValue = 0;
        tValue += ((pBuffer[pOffset + 0] & 0xff) << 8);
        tValue += ((pBuffer[pOffset + 1] & 0xff) << 0);
        return tValue;
    }

    public static void int2Buffer(int pValue, byte[] pBuffer, int pOffset) {
        pBuffer[pOffset + 0] = (byte) (pValue >>> 24);
        pBuffer[pOffset + 1] = (byte) (pValue >>> 16);
        pBuffer[pOffset + 2] = (byte) (pValue >>> 8);
        pBuffer[pOffset + 3] = (byte) (pValue >>> 0);
    }

    public static int buffer2Int(byte[] pBuffer, int pOffset) {
        int tValue = 0;
        tValue += ((pBuffer[pOffset + 0] & 0xff) << 24);
        tValue += ((pBuffer[pOffset + 1] & 0xff) << 16);
        tValue += ((pBuffer[pOffset + 2] & 0xff) << 8);
        tValue += ((pBuffer[pOffset + 3] & 0xff) << 0);
        return tValue;
    }

    public static void long2Buffer(long pValue, byte[] pBuffer, int pOffset) {
        pBuffer[pOffset + 0] = (byte) (pValue >>> 56);
        pBuffer[pOffset + 1] = (byte) (pValue >>> 48);
        pBuffer[pOffset + 2] = (byte) (pValue >>> 40);
        pBuffer[pOffset + 3] = (byte) (pValue >>> 32);
        pBuffer[pOffset + 4] = (byte) (pValue >>> 24);
        pBuffer[pOffset + 5] = (byte) (pValue >>> 16);
        pBuffer[pOffset + 6] = (byte) (pValue >>> 8);
        pBuffer[pOffset + 7] = (byte) (pValue >>> 0);
    }

    public static long buffer2Long(byte[] pBuffer, int pOffset) {
        long tValue = 0;
        tValue += (long) ((long) (pBuffer[pOffset + 0] & 0xff) << 56);
        tValue += (long) ((long) (pBuffer[pOffset + 1] & 0xff) << 48);
        tValue += (long) ((long) (pBuffer[pOffset + 2] & 0xff) << 40);
        tValue += (long) ((long) (pBuffer[pOffset + 3] & 0xff) << 32);
        tValue += (long) ((long) (pBuffer[pOffset + 4] & 0xff) << 24);
        tValue += (long) ((long) (pBuffer[pOffset + 5] & 0xff) << 16);
        tValue += (long) ((long) (pBuffer[pOffset + 6] & 0xff) << 8);
        tValue += (long) ((long) (pBuffer[pOffset + 7] & 0xff) << 0);
        return tValue;
    }
}
