package com.ulyp.database.util;

import com.ulyp.storage.util.ByteUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.junit.Assert;
import org.junit.Test;

public class ByteUtilsTest {

    @Test
    public void test() {
        LongList r = new LongArrayList();
        r.add(5L);
        r.add(Long.MAX_VALUE);
        r.add(42L);
        r.add(Long.MIN_VALUE);

        byte[] bytes = ByteUtils.longsToBytes(r);

        LongList longs = ByteUtils.bytesToLongs(bytes);

        Assert.assertEquals(longs, r);
    }
}