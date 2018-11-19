package com.hoddmimes.tcpip.impl;

import java.io.InputStream;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZInputStream;

public class CompressionInputStream extends ZInputStream {

	public CompressionInputStream(InputStream pIn, int pLevel) {
		super(pIn, pLevel);
		this.setFlushMode(JZlib.Z_SYNC_FLUSH);
	}

	public CompressionInputStream(InputStream pIn) {
		super(pIn);
		this.setFlushMode(JZlib.Z_SYNC_FLUSH);
	}

}
