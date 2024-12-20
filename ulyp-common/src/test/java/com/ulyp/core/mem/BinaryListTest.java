package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.bytes.BufferedOutputStream;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.DirectBytesIn;
import com.ulyp.core.bytes.PagedMemBytesOut;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BinaryListTest {

    private MemPageAllocator allocator() {
        return new MemPageAllocator() {

            @Override
            public MemPage allocate() {
                return new MemPage(0, new UnsafeBuffer(new byte[PageConstants.PAGE_SIZE]));
            }

            @Override
            public void deallocate(MemPage page) {

            }
        };
    }

    @Test
    void testBasicSize() {
        OutputBytesList out = new OutputBytesList(SerializedRecordedMethodCallList.WIRE_ID, new PagedMemBytesOut(allocator()));

        OutputBytesList.Writer writer = out.writer();
        writer.write("AVBACAS");
        writer.commit();

        InputBytesList inputList = out.flip();

        assertEquals(1, inputList.size());
    }

    @Test
    void testWriteByUsingWriter() {
        OutputBytesList out = new OutputBytesList(SerializedRecordedMethodCallList.WIRE_ID, new PagedMemBytesOut(allocator()));

        OutputBytesList.Writer writer = out.writer();
        writer.write("AVBACAS");
        writer.commit();

        InputBytesList inputList = out.flip();

        BytesIn in = inputList.iterator().next();
        assertEquals("AVBACAS", in.readString());
    }

    @Test
    void testByAddressAccess() throws IOException {
        OutputBytesList bytesOut = new OutputBytesList(SerializedRecordedMethodCallList.WIRE_ID, new PagedMemBytesOut(allocator()));

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
        InputBytesList inputList = bytesOut.flip();

        AddressableItemIterator<BytesIn> it = inputList.iterator();

        it.next();

        it.next();
        long address2 = it.address();

        UnsafeBuffer unsafeBuffer = new UnsafeBuffer(byteArray, (int) address2, 12);
        DirectBytesIn in = new DirectBytesIn(unsafeBuffer);

        assertFalse(in.readBoolean());
        assertEquals(9873434443L, in.readLong());
    }

    @Test
    void testSimpleReadWrite() {
        OutputBytesList bytesOut = new OutputBytesList(SerializedRecordedMethodCallList.WIRE_ID, new PagedMemBytesOut(allocator()));

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

        InputBytesList inputList = bytesOut.flip();

        assertEquals(3, inputList.size());

        AddressableItemIterator<BytesIn> iterator = inputList.iterator();

        Assertions.assertTrue(iterator.hasNext());

        BytesIn next = iterator.next();
        assertEquals(2, next.available());
        assertEquals('A', next.readChar());

        assertEquals(OutputBytesList.HEADER_LENGTH + 4, iterator.address());

        next = iterator.next();
        assertEquals(4, next.available());
        assertEquals('B', next.readChar());
        assertEquals('C', next.readChar());

        assertEquals(OutputBytesList.HEADER_LENGTH + 10, iterator.address());

        next = iterator.next();
        assertEquals(2, next.available());
        assertEquals('D', next.readChar());

        assertEquals(OutputBytesList.HEADER_LENGTH + 18, iterator.address());

        assertFalse(iterator.hasNext());
    }
}
