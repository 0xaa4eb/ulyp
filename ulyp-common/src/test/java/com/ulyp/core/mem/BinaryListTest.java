package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.bytes.BinaryInput;
import com.ulyp.core.bytes.BufferBinaryInput;
import com.ulyp.core.bytes.BufferedOutputStream;
import com.ulyp.core.bytes.PagedMemBinaryOutput;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class BinaryListTest {

    private MemPageAllocator allocator() {
        return new MemPageAllocator() {

            @Override
            public MemPage allocate() {
                return new MemPage(0, new UnsafeBuffer(new byte[MemPool.PAGE_SIZE]));
            }

            @Override
            public void deallocate(MemPage page) {

            }
        };
    }

    @Test
    public void test() throws IOException {
        BinaryList.Out out = new BinaryList.Out(RecordedMethodCallList.WIRE_ID, new PagedMemBinaryOutput(allocator()));

        out.add(o -> o.write("AVBACAS"));

        BinaryList.In inputList = out.flip();

        Assert.assertEquals(1, inputList.size());
    }

    @Test
    public void testByAddressAccess() throws IOException {
        BinaryList.Out bytesOut = new BinaryList.Out(RecordedMethodCallList.WIRE_ID, new PagedMemBinaryOutput(allocator()));

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

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bytesOut.writeTo(new BufferedOutputStream(outputStream));
        byte[] byteArray = outputStream.toByteArray();
        BinaryList.In inputList = bytesOut.flip();

        AddressableItemIterator<BinaryInput> it = inputList.iterator();

        BinaryInput next = it.next();
        long address1 = it.address();
        System.out.println(address1);

        BinaryInput next1 = it.next();
        long address2 = it.address();

        UnsafeBuffer unsafeBuffer = new UnsafeBuffer(byteArray, (int) address2, 12);
        BufferBinaryInput in = new BufferBinaryInput(unsafeBuffer);

        assertFalse(in.readBoolean());
        assertEquals(9873434443L, in.readLong());
    }

    @Test
    public void testSimpleReadWrite() {
        BinaryList.Out bytesOut = new BinaryList.Out(RecordedMethodCallList.WIRE_ID, new PagedMemBinaryOutput(allocator()));

        bytesOut.add(out -> {
            out.write('A');
        });
        bytesOut.add(out -> {
            out.write('B');
            out.write('C');
        });
        bytesOut.add(out -> {
            out.write('D');
        });

        BinaryList.In inputList = bytesOut.flip();

        assertEquals(3, inputList.size());

        AddressableItemIterator<BinaryInput> iterator = inputList.iterator();

        assertTrue(iterator.hasNext());

        BinaryInput next = iterator.next();
        assertEquals(2, next.available());
        assertEquals('A', next.readChar());

        assertEquals(BinaryList.HEADER_LENGTH + 4, iterator.address());

        next = iterator.next();
        assertEquals(4, next.available());
        assertEquals('B', next.readChar());
        assertEquals('C', next.readChar());

        assertEquals(BinaryList.HEADER_LENGTH + 10, iterator.address());

        next = iterator.next();
        assertEquals(2, next.available());
        assertEquals('D', next.readChar());

        assertEquals(BinaryList.HEADER_LENGTH + 18, iterator.address());

        assertFalse(iterator.hasNext());
    }
}
