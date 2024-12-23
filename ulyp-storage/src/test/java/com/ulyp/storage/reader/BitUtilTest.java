package com.ulyp.storage.reader;

import com.ulyp.core.util.BitUtil;
import org.agrona.collections.LongArrayList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BitUtilTest {

    @Test
    void testBytesToLong() {
        LongArrayList r = new LongArrayList();
        r.add(5L);
        r.add(Long.MAX_VALUE);
        r.add(42L);
        r.add(Long.MIN_VALUE);

        byte[] bytes = BitUtil.longsToBytes(r);

        LongArrayList longs = BitUtil.bytesToLongs(bytes);

        assertEquals(longs, r);
    }

    @Test
    void testBytesToInt() {
        byte[] buf = new byte[4];
        BitUtil.intToBytes(4523432, buf, 0);

        assertEquals(4523432, BitUtil.bytesToInt(buf, 0));
    }
}