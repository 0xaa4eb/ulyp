package com.ulyp.core.recorders.bytes;

import com.ulyp.core.bytes.BufferBytesOut;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.mem.PageConstants;
import org.agrona.concurrent.UnsafeBuffer;

public class BufferBytesOutTest extends BytesInOutTest {

    @Override
    protected BytesOut create() {
        return new BufferBytesOut(new UnsafeBuffer(new byte[PageConstants.PAGE_SIZE * 31]));
    }
}