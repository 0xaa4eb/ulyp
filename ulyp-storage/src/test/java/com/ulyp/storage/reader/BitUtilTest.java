package com.ulyp.storage.reader;

import com.ulyp.core.util.BitUtil;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BitUtilTest {

    @Test
    void testBytesToLong() {
        LongList r = new LongArrayList();
        r.add(5L);
        r.add(Long.MAX_VALUE);
        r.add(42L);
        r.add(Long.MIN_VALUE);

        byte[] bytes = BitUtil.longsToBytes(r);

        LongList longs = BitUtil.bytesToLongs(bytes);

        assertEquals(longs, r);
    }

    @Test
    void testBytesToInt() {
        byte[] buf = new byte[4];
        BitUtil.intToBytes(4523432, buf, 0);

        assertEquals(4523432, BitUtil.bytesToInt(buf, 0));
    }
}