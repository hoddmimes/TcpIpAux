package com.hoddmimes.tcpip.messages;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MessageDecoder {
	private ByteBuffer mBuffer;

	public MessageDecoder(byte[] pBuffer) {
		mBuffer = ByteBuffer.wrap(pBuffer);
	}

	public boolean readBoolean() {
		if (mBuffer.get() == (byte) 0) {
			return false;
		}
		return true;
	}

	public byte readByte() {
		return mBuffer.get();
	}

	public char readChar() {
		return (char) mBuffer.getShort();
	}

	public short readShort() {
		return mBuffer.getShort();
	}

	public int readInt() {
		return mBuffer.getInt();
	}

	public long readLong() {
		return mBuffer.getLong();
	}

	public float readFloat() {
		return mBuffer.getFloat();
	}

	public double readDouble() {
		return mBuffer.getDouble();
	}

	public byte[] readBytes() {
		if (!readBoolean()) {
			return null;
		}
		int tSize = mBuffer.getInt();
		byte[] tByteArray = new byte[tSize];
		mBuffer.get(tByteArray);
		return tByteArray;
	}

	public String readString() {
		if (!readBoolean()) {
			return null;
		}
		int tSize = mBuffer.getInt();
		if (tSize == 0) {
			return new String("");
		}

		byte[] tByteArray = new byte[tSize];
		mBuffer.get(tByteArray);
		return new String(tByteArray);
	}

	public BigInteger readBigInteger()
	{
		String tValueString = readString();
		return (tValueString == null) ? null : new BigInteger(tValueString);
	}
}
