package com.ulyp.core.recorders.bytes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Test;

import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.bytes.Mark;
import com.ulyp.core.mem.PageConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public abstract class BytesInOutTest {

    public static final int ITERATIONS = 100;

    protected abstract BytesOut create();

    @Test
    public void testReadWriteInts() {
        for (int i = 0; i < ITERATIONS; i++) {
            List<Object> written = new ArrayList<>();
            BytesOut out = create();

            while (out.position() < PageConstants.PAGE_SIZE * 30) {
                int rnd = ThreadLocalRandom.current().nextInt(5);
                if (rnd == 0) {
                    char value = (char) ('A' + ThreadLocalRandom.current().nextInt(25));
                    out.write(value);
                    written.add(value);
                } else if (rnd == 1) {
                    int value = ThreadLocalRandom.current().nextInt();
                    out.write(value);
                    written.add(value);
                } else if (rnd == 3) {
                    boolean value = ThreadLocalRandom.current().nextBoolean();
                    out.write(value);
                    written.add(value);
                } else {
                    byte value = (byte) ThreadLocalRandom.current().nextInt(128);
                    out.write(value);
                    written.add(value);
                }
            }

            BytesIn input = out.flip();

            for (Object value : written) {
                if (value instanceof Character) {
                    Assert.assertEquals(value, input.readChar());
                } else if (value instanceof Integer) {
                    Assert.assertEquals(value, input.readInt());
                } else if (value instanceof Long) {
                    Assert.assertEquals(value, input.readLong());
                } else if (value instanceof Boolean) {
                    Assert.assertEquals(value, input.readBoolean());
                } else if (value instanceof String) {
                    Assert.assertEquals(value, input.readString());
                } else {
                    Assert.assertEquals(value, input.readByte());
                }
            }
        }
    }

    @Test
    public void testReadWriteVariousObjects() {
        for (int i = 0; i < ITERATIONS; i++) {
            List<Object> written = new ArrayList<>();
            BytesOut out = create();

            while (out.position() < PageConstants.PAGE_SIZE * 30) {
                int rnd = ThreadLocalRandom.current().nextInt(8);
                if (rnd == 0) {
                    char value = (char) ('A' + ThreadLocalRandom.current().nextInt(25));
                    out.write(value);
                    written.add(value);
                } else if (rnd == 1) {
                    int value = ThreadLocalRandom.current().nextInt();
                    out.write(value);
                    written.add(value);
                } else if (rnd == 2) {
                    long value = ThreadLocalRandom.current().nextLong();
                    out.write(value);
                    written.add(value);
                } else if (rnd == 3) {
                    boolean value = ThreadLocalRandom.current().nextBoolean();
                    out.write(value);
                    written.add(value);
                } else if (rnd == 4) {
                    byte value = (byte) ThreadLocalRandom.current().nextInt(128);
                    out.write(value);
                    written.add(value);
                } else if (rnd == 5) {
                    String value = String.valueOf(ThreadLocalRandom.current().nextDouble());
                    out.write(value);
                    written.add(value);
                } else if (rnd == 6) {
                    byte[] byteArray = new byte[ThreadLocalRandom.current().nextInt(128) + 1];
                    ThreadLocalRandom.current().nextBytes(byteArray);
                    out.write(byteArray);
                    written.add(byteArray);
                } else {
                    byte[] byteArray = new byte[ThreadLocalRandom.current().nextInt(128) + 1];
                    ThreadLocalRandom.current().nextBytes(byteArray);
                    UnsafeBuffer buffer = new UnsafeBuffer(byteArray);
                    out.write(buffer);
                    written.add(buffer);
                }
            }

            BytesIn input = out.flip();

            for (Object value : written) {
                if (value instanceof Character) {
                    Assert.assertEquals(value, input.readChar());
                } else if (value instanceof Integer) {
                    Assert.assertEquals(value, input.readInt());
                } else if (value instanceof Long) {
                    Assert.assertEquals(value, input.readLong());
                } else if (value instanceof Boolean) {
                    Assert.assertEquals(value, input.readBoolean());
                } else if (value instanceof String) {
                    Assert.assertEquals(value, input.readString());
                } else if (value instanceof Byte) {
                    Assert.assertEquals(value, input.readByte());
                } else if (value instanceof byte[]) {
                    assertBytesEquals((byte[]) value, input.readBytes());
                } else if (value instanceof UnsafeBuffer) {
                    input.readBytes();
                }
            }
        }
    }

    @Test
    public void testReadWriteManyInts() {
        testWriteManyInts(1);
        testWriteManyInts(10);
        testWriteManyInts(50);
        testWriteManyInts(100);
        testWriteManyInts(512);
        testWriteManyInts(1024);
        testWriteManyInts(PageConstants.PAGE_SIZE / Integer.BYTES);
        testWriteManyInts(PageConstants.PAGE_SIZE / Integer.BYTES + 1);
        testWriteManyInts(2 * PageConstants.PAGE_SIZE / Integer.BYTES);
        testWriteManyInts(PageConstants.PAGE_SIZE / Integer.BYTES + 500);
        testWriteManyInts(10 * PageConstants.PAGE_SIZE / Integer.BYTES);
        testWriteManyInts(10 * PageConstants.PAGE_SIZE / Integer.BYTES + 3);
    }

    @Test
    public void testReadWriteAtArbitraryPosAllAddresses() {
        testReadWriteAtArbitraryPosAllAddresses(0);
        testReadWriteAtArbitraryPosAllAddresses(1);
        testReadWriteAtArbitraryPosAllAddresses(2);
        testReadWriteAtArbitraryPosAllAddresses(3);
    }

    private void testReadWriteAtArbitraryPosAllAddresses(int shift) {
        int writesCount = 3 * PageConstants.PAGE_SIZE / Integer.BYTES + 100;
        BytesOut out = create();

        for (int b = 0; b < shift; b++) {
            out.write((byte) 5);
        }
        int[] offsets = new int[writesCount];
        for (int i = 0; i < writesCount; i++) {
            offsets[i] = out.position();
            out.write(i);
        }
        for (int i = 0; i < writesCount; i++) {
            out.writeAt(offsets[i], i + 1);
        }

         BytesIn input = out.flip();

        for (int b = 0; b < shift; b++) {
            input.readByte();
        }
        for (int i = 0; i < writesCount; i++) {
            Assert.assertEquals(i + 1, input.readInt());
        }
    }

    @Test
    public void testReadWriteAtArbitraryPos() {
        int intsPerPage = PageConstants.PAGE_SIZE / Integer.BYTES;
        BytesOut out = create();
        for (int i = 0; i < intsPerPage - 1; i++) {
            out.write(i);
        }
        out.write((byte) 5);

        int offset = out.position();

        out.write(42);

        out.writeAt(offset, 55);

        BytesIn input = out.flip();
        for (int i = 0; i < intsPerPage - 1; i++) {
            input.readInt();
        }
        input.readByte();
        Assert.assertEquals(55, input.readInt());
    }

    @Test
    public void testByteArrayWriteSpanMultiplePages() {
        BytesOut out = create();
        int intsCount = (PageConstants.PAGE_SIZE / (Integer.BYTES * 2)) + 1;
        for (int i = 0; i < intsCount; i++) {
            out.write(i);
        }
        byte[] bytes = new byte[PageConstants.PAGE_SIZE * 2 + 532];
        ThreadLocalRandom.current().nextBytes(bytes);
        out.write(bytes);

        BytesIn input = out.flip();

        for (int i = 0; i < intsCount; i++) {
            assertEquals(i, input.readInt());
        }
        assertBytesEquals(bytes, input.readBytes());
    }

    @Test
    public void testReadWriteBytes() {
        BytesOut out = create();
        byte[] buf = new byte[] {5, 2, 127, -128, -120, 0, 5, 120, 54};
        try (BytesOut nestedOut = out.nest()) {
            nestedOut.write(new UnsafeBuffer(buf));
        }

        BytesIn bytesInResult = out.flip().readBytes();
        for (int i = 0; i < buf.length; i++) {
            Assert.assertEquals(buf[i], bytesInResult.readByte());
        }
    }

    @Test
    public void testSimpleReadWriteString() {
        BytesOut out = create();

        out.write("abc");

        assertEquals("abc", out.flip().readString());
    }

    @Test
    public void testNestedReadWriteString() {
        BytesOut out = create();

        try (BytesOut nested = out.nest()) {
            nested.write("abc");
        }

        assertEquals("abc", out.flip().readString());
    }


    @Test
    public void testRollingBack() {
        BytesOut out = create();

        try (BytesOut nested1 = out.nest()) {
            Mark mark = nested1.mark();
            nested1.write("abc");
            mark.rollback();

            nested1.write("xyz");
        }

        assertEquals("xyz", out.flip().readString());
    }

    @Test
    public void testSimpleReadWriteUtf8String() {
        BytesOut out = create();

        try (BytesOut nested = out.nest()) {
            nested.write("АБЦ");
        }

        assertEquals("АБЦ", out.flip().readString());
    }

    @Test
    public void testSimpleReadWriteUtf8StringChineese() {
        BytesOut out = create();

        try (BytesOut nested = out.nest()) {
            nested.write("早上好");
        }

        assertEquals("早上好", out.flip().readString());
    }

    @Test
    public void testComplexReadWrite() {
        BytesOut out = create();

        try (BytesOut nested = out.nest()) {
            nested.write(2L);
            nested.write((String) null);
            nested.write(6L);
        }

        BytesIn in = out.flip();

        assertEquals(2, in.readLong());
        assertNull(in.readString());
        assertEquals(6, in.readLong());
    }

    @Test
    public void testNestedAppender() {
        BytesOut out = create();

        try (BytesOut nested1 = out.nest()) {
            nested1.write(2L);
            try (BytesOut nested2 = nested1.nest()) {
                nested2.write("你吃了吗?");
            }
            nested1.write(6L);
            nested1.write((String) null);
        }

        BytesIn in = out.flip();

        assertEquals(2, in.readLong());
        assertEquals("你吃了吗?", in.readString());
        assertEquals(6, in.readLong());
        assertNull(in.readString());
    }

    @Test
    public void testNestedOutputsWithMarkRollbacks() {
        BytesOut out = create();

        try (BytesOut nested1 = out.nest()) {
            Mark mark1 = nested1.mark();
            nested1.write(2L);
            try (BytesOut nested2 = nested1.nest()) {
                Mark mark2 = nested2.mark();
                nested2.write("你吃了吗?");
                mark2.rollback();
                nested2.write("VXCZZBNANSDNASD");
            }
            mark1.rollback();
            nested1.write(6L);
            nested1.write("NZXBCZXC");
        }

        BytesIn in = out.flip();

        assertEquals(6, in.readLong());
        assertEquals("NZXBCZXC", in.readString());
    }

    @Test
    public void testNestedOutputsWithMarkRollbacks2() {
        BytesOut out = create();

        try (BytesOut nested1 = out.nest()) {
            nested1.write(2L);
            try (BytesOut nested2 = nested1.nest()) {
                Mark mark2 = nested2.mark();
                nested2.write("你吃了吗?");
                mark2.rollback();
                nested2.write("VXCZZBNANSDNASD");
            }
            nested1.write(6L);
            nested1.write("NZXBCZXC");
        }

        BytesIn in = out.flip();

        assertEquals(2, in.readLong());
        assertEquals("VXCZZBNANSDNASD", in.readString());
        assertEquals(6, in.readLong());
        assertEquals("NZXBCZXC", in.readString());
    }

    private void testWriteManyInts(int intsCount) {
        BytesOut out = create();
        for (int i = 0; i < intsCount; i++) {
            out.write(i);
        }
        BytesIn input = out.flip();

        assertEquals(intsCount * Integer.BYTES, input.available());
        for (int i = 0; i < intsCount; i++) {
            assertEquals(i, input.readInt());
        }
    }

    private static void assertBytesEquals(byte[] expected, BytesIn actual) {
        for (byte b : expected) {
            Assert.assertEquals(b, actual.readByte());
        }
    }
}