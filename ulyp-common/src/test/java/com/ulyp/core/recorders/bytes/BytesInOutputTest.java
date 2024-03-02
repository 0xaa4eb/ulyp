package com.ulyp.core.recorders.bytes;

import com.ulyp.core.bytes.*;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BytesInOutputTest {

    private final UnsafeBuffer buffer = new UnsafeBuffer(new byte[16 * 1024]);
    private final BytesOut out = new BufferBytesOut(buffer);
    private final BytesIn in = new DirectBytesIn(buffer);

    @Test
    public void testReadWriteBytes() {
        byte[] buf = new byte[] {5, 2, 127, -128, -120, 0, 5, 120, 54};
        try (BytesOut nestedOut = out.nest()) {
            nestedOut.write(new UnsafeBuffer(buf));
        }

        BytesIn bytesInResult = in.readBytes();
        for (int i = 0; i < buf.length; i++) {
            Assert.assertEquals(buf[i], bytesInResult.readByte());
        }
    }

    @Test
    public void testSimpleReadWriteString() {
        out.write("abc");

        assertEquals("abc", in.readString());
    }

    @Test
    public void testNestedReadWriteString() {
        try (BytesOut nested = out.nest()) {
            nested.write("abc");
        }

        assertEquals("abc", in.readString());
    }


    @Test
    public void testRollingBack() {
        try (BytesOut nested1 = out.nest()) {
            Mark mark = nested1.mark();
            nested1.write("abc");
            mark.rollback();

            nested1.write("xyz");
        }

        assertEquals("xyz", in.readString());
    }

    @Test
    public void testSimpleReadWriteUtf8String() {
        try (BytesOut nested = out.nest()) {
            nested.write("АБЦ");
        }

        assertEquals("АБЦ", in.readString());
    }

    @Test
    public void testSimpleReadWriteUtf8StringChineese() {
        try (BytesOut nested = out.nest()) {
            nested.write("早上好");
        }

        assertEquals("早上好", in.readString());
    }

    @Test
    public void testComplexReadWrite() {
        try (BytesOut nested = out.nest()) {
            nested.write(2L);
            nested.write((String) null);
            nested.write(6L);
        }

        assertEquals(2, in.readLong());
        assertNull(in.readString());
        assertEquals(6, in.readLong());
    }

    @Test
    public void testNestedAppender() {
        try (BytesOut nested1 = out.nest()) {
            nested1.write(2L);
            try (BytesOut nested2 = nested1.nest()) {
                nested2.write("你吃了吗?");
            }
            nested1.write(6L);
            nested1.write((String) null);
        }

        assertEquals(2, in.readLong());
        assertEquals("你吃了吗?", in.readString());
        assertEquals(6, in.readLong());
        assertNull(in.readString());
    }
}
