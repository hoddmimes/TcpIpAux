package com.hoddmimes.tcpip.impl;

import java.io.OutputStream;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZOutputStream;

public class CompressionOutputStream extends ZOutputStream {

	public CompressionOutputStream(OutputStream pOutputStream) {
		this(pOutputStream, JZlib.Z_DEFAULT_COMPRESSION);
	}

	public CompressionOutputStream(OutputStream pOutputStream, int pCompressionLevel) {
		super(pOutputStream, pCompressionLevel);
		this.setFlushMode(JZlib.Z_SYNC_FLUSH);
	}

}
