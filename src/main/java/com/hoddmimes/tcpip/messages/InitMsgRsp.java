package com.hoddmimes.tcpip.messages;

import com.hoddmimes.tcpip.impl.Version;

import java.math.BigInteger;

public class InitMsgRsp
{
    private int                     mVersion;
    private BigInteger              mCrypt_ClientPubKey;
    private boolean                 mSucces;
    private String                  mErrorText;


    public InitMsgRsp( BigInteger pCrypt_ClientPubKey, boolean pSuccess, String pErrorText )
    {
        mVersion = Version.VERSION;
        mCrypt_ClientPubKey = pCrypt_ClientPubKey;
        mSucces = pSuccess;
        mErrorText = pErrorText;
    }


    public InitMsgRsp(MessageDecoder pDecoder)
    {
        decode( pDecoder );
    }


    public byte[] encode() {
       MessageEncoder tEncoder = new MessageEncoder();
       tEncoder.add( mVersion );
       tEncoder.add( mCrypt_ClientPubKey );
       tEncoder.add( mSucces );
       tEncoder.add( mErrorText );
       return tEncoder.getBytes();
    }

    public void decode( MessageDecoder pDecoder ) {
        mVersion = pDecoder.readInt();
        mCrypt_ClientPubKey = pDecoder.readBigInteger();
        mSucces = pDecoder.readBoolean();
        mErrorText = pDecoder.readString();
    }

    public int getVersion() {
        return mVersion;
    }

    public BigInteger getClientPubKey() {
        return mCrypt_ClientPubKey;
    }

    public boolean isAccepted() {
        return mSucces;
    }

    public String getErrorText() {
        return mErrorText;
    }
}
