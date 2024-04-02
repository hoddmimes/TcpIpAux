package com.hoddmimes.tcpip.crypto;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
public class DynamicByteBuffer implements Comparable<ByteBuffer> {
        private ByteBuffer mBuffer;

        public DynamicByteBuffer(int initialCapacity ) {
            this.mBuffer = ByteBuffer.allocate(initialCapacity);
        }

        public int capacity() {
            return mBuffer.capacity();
        }

        public void clear() {
            mBuffer.clear();
        }

        public Buffer flip() {
            return mBuffer.flip();
        }

        public boolean hasRemaining() {
            return mBuffer.hasRemaining();
        }

        public boolean isReadOnly() {
            return mBuffer.isReadOnly();
        }

        public int limit() {
            return mBuffer.limit();
        }

        public Buffer limit(int newLimit) {
            return mBuffer.limit(newLimit);
        }

        public Buffer mark() {
            return mBuffer.mark();
        }

        public int position() {
            return mBuffer.position();
        }

        public Buffer position(int newPosition) {
            return mBuffer.position(newPosition);
        }

        public int remaining() {
            return mBuffer.remaining();
        }

        public Buffer reset() {
            return mBuffer.reset();
        }

        public Buffer rewind() {
            return mBuffer.rewind();
        }

        public byte[] array() {
            return mBuffer.array();
        }

        public int arrayOffset() {
            return mBuffer.arrayOffset();
        }

        public ByteBuffer compact() {
            return mBuffer.compact();
        }

        public int compareTo(ByteBuffer that) {
            return mBuffer.compareTo(that);
        }

        public ByteBuffer duplicate() {
            return mBuffer.duplicate();
        }

        public boolean equals(Object ob) {
            return mBuffer.equals(ob);
        }

        public byte get() {
            return mBuffer.get();
        }

        public ByteBuffer get(byte[] dst) {
            return mBuffer.get(dst);
        }

        public ByteBuffer get(byte[] dst, int offset, int length) {
            return mBuffer.get(dst, offset, length);
        }

        public byte get(int index) {
            return mBuffer.get(index);
        }

        public char getChar() {
            return mBuffer.getChar();
        }

        public char getChar(int index) {
            return mBuffer.getChar(index);
        }

        public double getDouble() {
            return mBuffer.getDouble();
        }

        public double getDouble(int index) {
            return mBuffer.getDouble(index);
        }

        public float getFloat() {
            return mBuffer.getFloat();
        }

        public float getFloat(int index) {
            return mBuffer.getFloat(index);
        }

        public int getInt() {
            return mBuffer.getInt();
        }

        public int getInt(int index) {
            return mBuffer.getInt(index);
        }

        public long getLong() {
            return mBuffer.getLong();
        }

        public long getLong(int index) {
            return mBuffer.getLong(index);
        }

        public short getShort() {
            return mBuffer.getShort();
        }

        public short getShort(int index) {
            return mBuffer.getShort(index);
        }

        public boolean hasArray() {
            return mBuffer.hasArray();
        }

        public boolean isDirect() {
            return mBuffer.isDirect();
        }

        public ByteOrder order() {
            return mBuffer.order();
        }

        public ByteBuffer order(ByteOrder bo) {
            return mBuffer.order(bo);
        }

        public void put(byte b) {
            ensureSpace(Byte.BYTES);
            mBuffer.put(b);
        }

        public void put(byte[] src) {
            ensureSpace(src.length);
            mBuffer.put(src);
        }

        public void put(byte[] src, int offset, int length) {
            ensureSpace(length);
            mBuffer.put(src, offset, length);
        }

        public void put(ByteBuffer src) {
            ensureSpace(src.remaining());
            mBuffer.put(src);
        }

        public void put(int index, byte b) {
            ensureSpace(Byte.BYTES);
            mBuffer.put(index, b);
        }

        public void putChar(char value) {
            ensureSpace(Character.BYTES);
            mBuffer.putChar(value);
        }

        public void putChar(int index, char value) {
            ensureSpace(Character.BYTES);
             mBuffer.putChar(index, value);
        }

        public void putDouble(double value) {
            ensureSpace(Double.BYTES);
             mBuffer.putDouble(value);
        }

        public void putDouble(int index, double value) {
            ensureSpace(Double.BYTES);
             mBuffer.putDouble(index, value);
        }

        public ByteBuffer putFloat(float value) {
            ensureSpace(4);
            return mBuffer.putFloat(value);
        }

        public void putFloat(int index, float value) {
            ensureSpace(Float.BYTES);
            mBuffer.putFloat(index, value);
        }

        public void putInt(int value) {
            ensureSpace(Integer.BYTES);
             mBuffer.putInt(value);
        }

        public void putInt(int index, int value) {
            ensureSpace(Integer.BYTES);
             mBuffer.putInt(index, value);
        }

        public void putLong(int index, long value) {
            ensureSpace(Long.BYTES);
             mBuffer.putLong(index, value);
        }

        public void putLong(long value) {
            ensureSpace(Long.BYTES);
            mBuffer.putLong(value);
        }

        public void putShort(int index, short value) {
            ensureSpace(Short.BYTES);
             mBuffer.putShort(index, value);
        }

        public void putShort(short value) {
            ensureSpace(Short.BYTES);
            mBuffer.putShort(value);
        }

        public ByteBuffer slice() {
            return mBuffer.slice();
        }

        @Override
        public int hashCode() {
            return mBuffer.hashCode();
        }

        @Override
        public String toString() {
            return mBuffer.toString();
        }

        public ByteBuffer getByteBuffer() {
            return mBuffer;
        }


        private void ensureSpace(int needed) {
            if (remaining() >= needed) {
                return;
            }

            int newCapacity = (int) (mBuffer.capacity() + 1024);
            while (newCapacity < (mBuffer.capacity() + needed)) {
                newCapacity += 1024;
            }

            ByteBuffer expanded = ByteBuffer.allocate(newCapacity);
            expanded.order(mBuffer.order());
            mBuffer.flip();
            expanded.put(mBuffer);
            mBuffer = expanded;
        }
}
