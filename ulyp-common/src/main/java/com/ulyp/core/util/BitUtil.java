package com.ulyp.core.util;

import org.agrona.collections.LongArrayList;

public class BitUtil {

    public static int log2(int value) {
        if (!org.agrona.BitUtil.isPowerOfTwo(value)) {
            throw new IllegalArgumentException("is not power of 2: " + value);
        }
        return (int) (Math.log(value) / Math.log(2));
    }

    public static void intToBytes(long value, byte[] dst, int offset) {
        for (int i = Integer.BYTES - 1; i >= 0; i--) {
            dst[offset + i] = (byte) (value & 0xFF);
            value >>= Byte.SIZE;
        }
    }

    public static void longToBytes(long value, byte[] dst, int offset) {
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            dst[offset + i] = (byte) (value & 0xFF);
            value >>= Byte.SIZE;
        }
    }

    public static byte[] longsToBytes(LongArrayList list) {
        byte[] result = new byte[Long.BYTES * list.size()];
        for (int i = 0; i < list.size(); i++) {
            longToBytes(list.getLong(i), result, i * Long.BYTES);
        }
        return result;
    }

    public static long bytesToLong(final byte[] bytes, int offset) {
        long result = 0;
        for (int i = 0; i < Long.BYTES; i++) {
            result <<= Byte.SIZE;
            result |= (bytes[offset + i] & 0xFF);
        }
        return result;
    }

    public static int bytesToInt(final byte[] bytes, int offset) {
        int result = 0;
        for (int i = 0; i < Integer.BYTES; i++) {
            result <<= Byte.SIZE;
            result |= (bytes[offset + i] & 0xFF);
        }
        return result;
    }


    public static LongArrayList bytesToLongs(final byte[] bytes) {
        int size = bytes.length / Long.BYTES;
        LongArrayList result = new LongArrayList(size, Long.MIN_VALUE);
        for (int i = 0; i < size; i++) {
            result.add(BitUtil.bytesToLong(bytes, i * Long.BYTES));
        }
        return result;
    }

    public static long longFromInts(int x, int y) {
        return  (((long)x) << 32) | (y & 0xffffffffL);
    }
}
