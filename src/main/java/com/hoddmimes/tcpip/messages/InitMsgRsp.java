package com.hoddmimes.tcpip.messages;

import com.hoddmimes.tcpip.impl.Version;

import java.math.BigInteger;

public class InitMsgRsp
{
    private int                     mVersion;
    private BigInteger              mDH_PubKey;
    private boolean                 mSuccess;
    private String                  mErrorText;



    public InitMsgRsp( BigInteger pDH_ServerPubKey, boolean pSuccess, String pErrorText, byte[] pAuthChallange )
    {
        mVersion = Version.VERSION;
        mDH_PubKey = pDH_ServerPubKey;
        mSuccess = pSuccess;
        mErrorText = pErrorText;
    }

    public InitMsgRsp( BigInteger pDH_ServerPubKey, boolean pSuccess, String pErrorText) {
        this( pDH_ServerPubKey, pSuccess, pErrorText, null);
    }


    public InitMsgRsp(MessageDecoder pDecoder)
    {
        decode( pDecoder );
    }


    public byte[] encode() {
       MessageEncoder tEncoder = new MessageEncoder();
       tEncoder.add( mVersion );
       tEncoder.add( mDH_PubKey );
       tEncoder.add( mSuccess );
       tEncoder.add( mErrorText );
       return tEncoder.getBytes();
    }

    public void decode( MessageDecoder pDecoder ) {
        mVersion = pDecoder.readInt();
        mDH_PubKey = pDecoder.readBigInteger();
        mSuccess = pDecoder.readBoolean();
        mErrorText = pDecoder.readString();
    }

    public int getVersion() {
        return mVersion;
    }

    public BigInteger getServerDHPubKey()  {
        return mDH_PubKey;
    }

    public boolean isAccepted() {
        return mSuccess;
    }

    public String getErrorText() {
        return mErrorText;
    }
}
