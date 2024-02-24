/*
package com.ulyp.core.mem;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Test;

public class RegionTest {

    @Test
    public void test() {
        System.out.println(Page.PAGE_BITS >> Page.PAGE_BITS);
        System.out.println((Page.PAGE_BYTE_SIZE) >> Page.PAGE_BITS);
        System.out.println((Page.PAGE_BYTE_SIZE + 10) >> Page.PAGE_BITS);
        System.out.println((Page.PAGE_BYTE_SIZE + Page.PAGE_BYTE_SIZE) >> Page.PAGE_BITS);
    }

    @Test
    public void testAllocateDeallocate() {
        Region region = new Region(5, new UnsafeBuffer(new byte[Page.PAGE_BYTE_SIZE * 4]), 4);

        Page p1 = region.allocate();
        Assert.assertNotNull(p1);
        Page p2 = region.allocate();
        Assert.assertNotNull(p2);
        Page p3 = region.allocate();
        Assert.assertNotNull(p3);
        Page p4 = region.allocate();
        Assert.assertNotNull(p4);

        Assert.assertNull(region.allocate());
        Assert.assertNull(region.allocate());
        Assert.assertNull(region.allocate());

        region.deallocate(p1);

        p1 = region.allocate();
        Assert.assertNotNull(p1);
    }
}*/
