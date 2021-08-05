package com.ulyp.database.util;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.junit.Assert;
import org.junit.Test;
import org.rocksdb.util.ByteUtil;

import static org.junit.Assert.*;

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