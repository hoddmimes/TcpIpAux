package com.hoddmimes.tcpip.messages;

import com.hoddmimes.tcpip.TcpIpConnectionTypes;
import com.hoddmimes.tcpip.impl.Version;

import java.math.BigInteger;

public class InitMsgRqst
{
    private int                     mVersion;
    private TcpIpConnectionTypes mConnectionType;
    private BigInteger              mCrypt_G;
    private BigInteger              mCrypt_P;
    private BigInteger              mCrypt_ClientPubKey;


    public InitMsgRqst( TcpIpConnectionTypes pConnectionType, BigInteger pCrypt_G, BigInteger pCrypt_P, BigInteger pCrypt_ClientPubKey )
    {
        mVersion = Version.VERSION;
        mConnectionType = pConnectionType;
        mCrypt_ClientPubKey = pCrypt_ClientPubKey;
        mCrypt_G = pCrypt_G;
        mCrypt_P = pCrypt_P;
    }


    public int getVersion() {
        return mVersion;
    }

    public BigInteger getClientPubKey() {
        return mCrypt_ClientPubKey;
    }

    public BigInteger getCryptG() {
        return mCrypt_G;
    }

    public BigInteger getCryptP() {
        return mCrypt_P;
    }

    public TcpIpConnectionTypes getConnectionType() {
        return mConnectionType;
    }


    public InitMsgRqst( MessageDecoder pDecoder)
    {
        decode( pDecoder );
    }

    public InitMsgRqst( TcpIpConnectionTypes pConnectionType ) {
        this( pConnectionType, null, null, null );
    }

    public byte[] encode() {
       MessageEncoder tEncoder = new MessageEncoder();
       tEncoder.add( mVersion );
       tEncoder.add( mConnectionType.encode());
       tEncoder.add( mCrypt_G );
       tEncoder.add( mCrypt_P );
       tEncoder.add( mCrypt_ClientPubKey );
       return tEncoder.getBytes();
    }

    public void decode( MessageDecoder pDecoder ) {
        mVersion = pDecoder.readInt();
        mConnectionType = TcpIpConnectionTypes.decode( pDecoder.readInt());
        mCrypt_G = pDecoder.readBigInteger();
        mCrypt_P = pDecoder.readBigInteger();
        mCrypt_ClientPubKey = pDecoder.readBigInteger();
    }
}
