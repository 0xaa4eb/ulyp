package com.ulyp.core.recorders.bytes;

import com.ulyp.core.mem.MemPool;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;

public class MemBinaryOutputTest {

    @Test
    public void test() throws IOException {
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

/*    @Test
    public void testByteArrayWriteSpanMultiplePages() throws IOException {
        TestPageAllocator pageAllocator = new TestPageAllocator();
        BinaryOutput out = new MemBinaryOutput(pageAllocator);
        int intsCount = (MemPool.PAGE_SIZE / (Integer.BYTES * 2))+ 1; *//* more than half of page *//*
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
        byte[] bytesRead = input.
    }*/

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

    private static BufferBinaryInput flip(BinaryOutput out) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int length = out.writeTo(outputStream);
        BufferBinaryInput input = new BufferBinaryInput(outputStream.toByteArray(), length);
        return input;
    }
}