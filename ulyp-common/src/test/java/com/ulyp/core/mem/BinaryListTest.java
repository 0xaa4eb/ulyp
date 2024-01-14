package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BufferBinaryInput;
import com.ulyp.core.recorders.bytes.BufferBinaryOutput;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import static org.junit.Assert.*;

public class BinaryListTest {

    private final byte[] buffer = new byte[8 * 1024];

    @Test
    public void testByAddressAccess() {

        BinaryList.Out bytesOut = new BinaryList.Out(5, new BufferBinaryOutput(new UnsafeBuffer(buffer)));

        bytesOut.add(out -> {
            out.write(true);
            out.write(5454534534L);
        });
        bytesOut.add(out -> {
            out.write(false);
            out.write(9873434443L);
        });
        bytesOut.add(out -> {
            out.write(true);
            out.write(1233434734L);
        });

        BinaryList.In bytesIn = new BinaryList.In(new BufferBinaryInput(buffer));

        AddressableItemIterator<BinaryInput> it = bytesIn.iterator();

        BinaryInput next = it.next();
        long address1 = it.address();
        System.out.println(address1);

        BinaryInput next1 = it.next();
        long address2 = it.address();

        UnsafeBuffer unsafeBuffer = new UnsafeBuffer(buffer, (int) address2, 12);
        BufferBinaryInput in = new BufferBinaryInput(unsafeBuffer);

        assertFalse(in.readBoolean());
        assertEquals(9873434443L, in.readLong());
    }

    @Test
    public void testSimpleReadWrite() {
        BinaryList.Out write = new BinaryList.Out(5435, new BufferBinaryOutput(new UnsafeBuffer(buffer)));

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

        BinaryList.In read = new BinaryList.In(new BufferBinaryInput(new UnsafeBuffer(buffer)));

        assertEquals(3, read.size());

        AddressableItemIterator<BinaryInput> iterator = read.iterator();

        assertTrue(iterator.hasNext());

        BinaryInput next = iterator.next();
        assertEquals(2, next.available());
        assertEquals('A', next.readChar());

        assertEquals(BinaryList.In.HEADER_LENGTH + 4, iterator.address());

        next = iterator.next();
        assertEquals(4, next.available());
        assertEquals('B', next.readChar());
        assertEquals('C', next.readChar());

        assertEquals(BinaryList.In.HEADER_LENGTH + 10, iterator.address());

        next = iterator.next();
        assertEquals(2, next.available());
        assertEquals('D', next.readChar());

        assertEquals(BinaryList.In.HEADER_LENGTH + 18, iterator.address());

        assertFalse(iterator.hasNext());
    }
}
