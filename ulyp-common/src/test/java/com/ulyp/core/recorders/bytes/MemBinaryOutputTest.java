package com.ulyp.core.recorders.bytes;

import com.ulyp.core.mem.MemPool;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;

public class MemBinaryOutputTest {

    public static final int ITERATIONS = 100;

    @Test
    public void testReadWriteInts() throws IOException {
        for (int i = 0; i < ITERATIONS; i++) {
            List<Object> written = new ArrayList<>();
            BinaryOutput out = new MemBinaryOutput(new TestPageAllocator());

            while (out.position() < MemPool.PAGE_SIZE * 30) {
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

            BufferBinaryInput input = flip(out);

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
    public void testReadWriteVariousObjects() throws IOException {
        for (int i = 0; i < ITERATIONS; i++) {
            List<Object> written = new ArrayList<>();
            BinaryOutput out = new MemBinaryOutput(new TestPageAllocator());

            while (out.position() < MemPool.PAGE_SIZE * 30) {
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

            BufferBinaryInput input = flip(out);

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
    public void testReadWriteManyInts() throws IOException {
        testWriteManyInts(10);
        testWriteManyInts(50);
        testWriteManyInts(100);
        testWriteManyInts(512);
        testWriteManyInts(1024);
        testWriteManyInts(MemPool.PAGE_SIZE / Integer.BYTES);
        testWriteManyInts(MemPool.PAGE_SIZE / Integer.BYTES + 1);
        testWriteManyInts(2 * MemPool.PAGE_SIZE / Integer.BYTES);
        testWriteManyInts(MemPool.PAGE_SIZE / Integer.BYTES + 500);
        testWriteManyInts(10 * MemPool.PAGE_SIZE / Integer.BYTES);
        testWriteManyInts(10 * MemPool.PAGE_SIZE / Integer.BYTES + 3);
    }

    @Test
    public void testReadWriteAtArbitraryPosAllAddresses() throws IOException {
        testReadWriteAtArbitraryPosAllAddresses(0);
        testReadWriteAtArbitraryPosAllAddresses(1);
        testReadWriteAtArbitraryPosAllAddresses(2);
        testReadWriteAtArbitraryPosAllAddresses(3);
    }

    private void testReadWriteAtArbitraryPosAllAddresses(int shift) throws IOException {
        int writesCount = 3 * MemPool.PAGE_SIZE / Integer.BYTES + 100;
        BinaryOutput out = new MemBinaryOutput(new TestPageAllocator());

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

        BufferBinaryInput input = flip(out);

        for (int b = 0; b < shift; b++) {
            input.readByte();
        }
        for (int i = 0; i < writesCount; i++) {
            Assert.assertEquals(i + 1, input.readInt());
        }
    }

    @Test
    public void testReadWriteAtArbitraryPos() throws IOException {
        int intsPerPage = MemPool.PAGE_SIZE / Integer.BYTES;
        BinaryOutput out = new MemBinaryOutput(new TestPageAllocator());
        for (int i = 0; i < intsPerPage - 1; i++) {
            out.write(i);
        }
        out.write((byte) 5);

        int offset = out.position();

        out.write(42);

        out.writeAt(offset, 55);

        BufferBinaryInput input = flip(out);
        for (int i = 0; i < intsPerPage - 1; i++) {
            input.readInt();
        }
        input.readByte();
        Assert.assertEquals(55, input.readInt());
    }

    @Test
    public void testByteArrayWriteSpanMultiplePages() throws IOException {
        BinaryOutput out = new MemBinaryOutput(new TestPageAllocator());
        int intsCount = (MemPool.PAGE_SIZE / (Integer.BYTES * 2)) + 1;
        for (int i = 0; i < intsCount; i++) {
            out.write(i);
        }
        byte[] bytes = new byte[MemPool.PAGE_SIZE * 2 + 532];
        ThreadLocalRandom.current().nextBytes(bytes);
        out.write(bytes);

        BufferBinaryInput input = flip(out);

        for (int i = 0; i < intsCount; i++) {
            assertEquals(i, input.readInt());
        }
        assertBytesEquals(bytes, input.readBytes());
    }

    private void testWriteManyInts(int intsCount) throws IOException {
        TestPageAllocator pageAllocator = new TestPageAllocator();
        BinaryOutput out = new MemBinaryOutput(pageAllocator);
        for (int i = 0; i < intsCount; i++) {
            out.write(i);
        }
        BufferBinaryInput input = flip(out);

        assertEquals(intsCount * Integer.BYTES, input.available());
        for (int i = 0; i < intsCount; i++) {
            assertEquals(i, input.readInt());
        }
    }

    private static void assertBytesEquals(byte[] expected, BinaryInput actual) {
        for (byte b : expected) {
            Assert.assertEquals(b, actual.readByte());
        }
    }

    private static BufferBinaryInput flip(BinaryOutput out) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int length = out.writeTo(outputStream);
        return new BufferBinaryInput(outputStream.toByteArray(), length);
    }
}