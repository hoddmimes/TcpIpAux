package com.hoddmimes.tcpip.messages;

public class IncompatibleConnectionException extends Exception
{
    public IncompatibleConnectionException( String pReason) {
        super( pReason );
    }
}
