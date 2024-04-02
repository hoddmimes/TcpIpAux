package com.hoddmimes.tcpip.messages;

import com.hoddmimes.tcpip.impl.Version;

import java.math.BigInteger;


/**
 * This message is sent from the client to the server as part of the connection establishment
 */

public class InitMsgRqst
{
    private int                     mVersion;
    private int                     mConnectionType;        // See TcpIpConnectionTypes
    private BigInteger              mCrypt_G;               // Agreed generator
    private BigInteger              mCrypt_P;               // Agreed prime
    private BigInteger              mDH_PubKey;             // Client public DH key
    private byte[]                  mEncodedPublicSSHKey;      // Client encoded public SSH key, null if ssh-auth is not enabled





    public InitMsgRqst(int pConnectionType, BigInteger pCrypt_G, BigInteger pCrypt_P, BigInteger pDH_PubKey, byte[] pEncodedPublicSSHKey )
    {
        mVersion = Version.VERSION;
        mConnectionType = pConnectionType;
        mDH_PubKey = pDH_PubKey;
        mCrypt_G = pCrypt_G;
        mCrypt_P = pCrypt_P;
        mEncodedPublicSSHKey = pEncodedPublicSSHKey;
    }


    public int getVersion() {
        return mVersion;
    }

    public BigInteger getClientDHPubKey() {
        return mDH_PubKey;
    }

    public BigInteger getCryptG() {
        return mCrypt_G;
    }

    public BigInteger getCryptP() {
        return mCrypt_P;
    }

    public int getConnectionType() {
        return mConnectionType;
    }

    public byte[] getEncodedPublicSSHKey() {
        return mEncodedPublicSSHKey;
    }


    public InitMsgRqst( MessageDecoder pDecoder )
    {
        decode( pDecoder );
    }

    public InitMsgRqst( int pConnectionType, byte[] pEncodedPublicSSHKey ) {
        this( pConnectionType, null, null, null, pEncodedPublicSSHKey );
    }

    public byte[] encode() {
       MessageEncoder tEncoder = new MessageEncoder();
       tEncoder.add( mVersion );
       tEncoder.add( mConnectionType);
       tEncoder.add( mCrypt_G );
       tEncoder.add( mCrypt_P );
       tEncoder.add( mDH_PubKey );
       tEncoder.add( mEncodedPublicSSHKey );
       return tEncoder.getBytes();
    }

    public void decode( MessageDecoder pDecoder ) {
        mVersion = pDecoder.readInt();
        mConnectionType = pDecoder.readInt();
        mCrypt_G = pDecoder.readBigInteger();
        mCrypt_P = pDecoder.readBigInteger();
        mDH_PubKey = pDecoder.readBigInteger();
        mEncodedPublicSSHKey = pDecoder.readBytes();
    }
}
