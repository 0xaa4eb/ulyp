package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BufferBinaryInput;
import com.ulyp.core.recorders.bytes.BufferBinaryOutput;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import static org.junit.Assert.*;

public class WriteBinaryListTest {

    private final byte[] buf = new byte[16 * 1024];

    @Test
    public void testSimpleReadWrite() {
        WriteBinaryList write = new WriteBinaryList(5435, new BufferBinaryOutput(new UnsafeBuffer(buf)));

        write.add(out -> {
            out.write('A');
        });
        write.add(out -> {
            out.write('B');
            out.write('C');
        });
        write.add(out -> {
            out.write('D');
        });

        ReadBinaryList read = new ReadBinaryList(new BufferBinaryInput(new UnsafeBuffer(buf)));

        assertEquals(3, read.size());

        AddressableItemIterator<BinaryInput> iterator = read.iterator();

        assertTrue(iterator.hasNext());

        BinaryInput next = iterator.next();
        assertEquals(2, next.available());
        assertEquals('A', next.readChar());

        assertEquals(ReadBinaryList.HEADER_LENGTH, iterator.address());

        next = iterator.next();
        assertEquals(4, next.available());
        assertEquals('B', next.readChar());
        assertEquals('C', next.readChar());

        assertEquals(ReadBinaryList.HEADER_LENGTH + 6, iterator.address());

        next = iterator.next();
        assertEquals(2, next.available());
        assertEquals('D', next.readChar());

        assertEquals(ReadBinaryList.HEADER_LENGTH + 14, iterator.address());

        assertFalse(iterator.hasNext());
    }
}