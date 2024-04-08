package com.ulyp.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class ConcurrentSimpleObjectPoolTest {

    @Test
    public void testReuseSameEntryOnSubsequentAccess() {
        Set<byte[]> allClaimedArrays = Collections.newSetFromMap(new IdentityHashMap<>());
        ConcurrentSimpleObjectPool<byte[]> buf = new ConcurrentSimpleObjectPool<>(8, () -> new byte[1024]);

        for (int i = 0; i < 1000; i++) {
            ConcurrentSimpleObjectPool.ObjectPoolClaim<byte[]> claim = buf.claim();
            allClaimedArrays.add(claim.get());
            claim.close();
        }

        Assert.assertEquals(1, allClaimedArrays.size());
    }
}
