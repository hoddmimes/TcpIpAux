package com.hoddmimes.tcpip.messages;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;

public class MessageEncoder {
	private final static int DEFAULT_BUFFER_SIZE = 1024;
	private ByteBuffer mBuffer;

	public MessageEncoder() {
		this(DEFAULT_BUFFER_SIZE);
	}

	public MessageEncoder(int pBufferSize) {
		mBuffer = ByteBuffer.allocate(pBufferSize);
	}

	private void ensureCapacity(int pSize) {
		if ((mBuffer.remaining() - pSize) < 0) {
			ByteBuffer tBuffer = null;
			int tAllocSize = 0;

			if (mBuffer.capacity() >= 262144) {
				tAllocSize = Math.max(262144, pSize);
			} else {
				tAllocSize = Math.max((2 * mBuffer.capacity()), pSize);
			}
			tBuffer = ByteBuffer.allocate(mBuffer.capacity() + tAllocSize);
			mBuffer.flip();
			tBuffer.put(mBuffer);
			mBuffer = null;
			mBuffer = tBuffer;
		}
	}

	public void add(boolean pValue) {
		if (pValue) {
			add((byte) 1);
		} else {
			add((byte) 0);
		}
	}

	public int getSize() {
		return mBuffer.position();
	}

	public void add(byte pValue) {
		ensureCapacity((Byte.SIZE / 8));
		mBuffer.put(pValue);
	}

	public void add(char pValue) {
		ensureCapacity((Short.SIZE / 8));
		mBuffer.putShort((short) pValue);
	}

	public void add(short pValue) {
		ensureCapacity((Short.SIZE / 8));
		mBuffer.putShort(pValue);
	}

	public void add(int pValue) {
		ensureCapacity((Integer.SIZE / 8));
		mBuffer.putInt(pValue);
	}

	public void add(long pValue) {
		ensureCapacity((Long.SIZE / 8));
		mBuffer.putLong(pValue);
	}

	public void add(float pValue) {
		ensureCapacity((Float.SIZE / 8));
		mBuffer.putFloat(pValue);
	}

	public void add(double pValue) {
		ensureCapacity((Double.SIZE / 8));
		mBuffer.putDouble(pValue);
	}

	public void add(byte[] pBuffer) {
		if (pBuffer == null) {
			add(false);
		} else {
			ensureCapacity(pBuffer.length + 5);
			add(true);
			add(pBuffer.length);
			mBuffer.put(pBuffer);
		}
	}

	public void add(BigInteger pValue) {
		if (pValue == null) {
			add(false);
		} else {
			add( pValue.toString(10));
		}
	}

	public void add(String pString) {
		if (pString == null) {
			add(false);
		} else {
			byte[] tBytes = pString.getBytes();
			ensureCapacity(tBytes.length + 5);
			add(true);
			add(tBytes.length);
			if (tBytes.length > 0) {
				mBuffer.put(tBytes);
			}
		}
	}

	public byte[] getBytes() {
		byte[] tDst = new byte[mBuffer.position()];
		byte[] tSrc = mBuffer.array();
		System.arraycopy(tSrc, 0, tDst, 0, mBuffer.position());
		return tDst;
	}
}
