package com.ulyp.core.recorders.bytes;

import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.bytes.PagedMemBytesOut;

public class PagedMemBytesOutTest extends BytesInOutTest {

    @Override
    protected BytesOut create() {
        return new PagedMemBytesOut(new TestPageAllocator());
    }
}