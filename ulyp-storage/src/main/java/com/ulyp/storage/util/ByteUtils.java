package com.ulyp.storage.util;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

public class ByteUtils {

    public static void longToBytes(long value, byte[] dst, int offset) {
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            dst[offset + i] = (byte) (value & 0xFF);
            value >>= Byte.SIZE;
        }
    }

    public static byte[] longsToBytes(LongList list) {
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

    public static LongList bytesToLongs(final byte[] bytes) {
        int size = bytes.length / Long.BYTES;
        LongList result = new LongArrayList(size);
        for (int i = 0; i < size; i++) {
            result.add(ByteUtils.bytesToLong(bytes, i * Long.BYTES));
        }
        return result;
    }
}
