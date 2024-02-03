package com.ulyp.storage.util;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.bytes.BinaryInput;
import com.ulyp.core.bytes.BufferBinaryInput;
import com.ulyp.core.bytes.BufferBinaryOutput;
import com.ulyp.storage.reader.BinaryListWithAddress;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class BinaryListFileWriterTest {

    private final byte[] buffer = new byte[16 * 1024];

    private BinaryListFileWriter writer;
    private BinaryListFileReader reader;
    private ByAddressFileReader byAddressFileReader;

    @Before
    public void setUp() throws IOException {
        File file = Files.createTempFile("BinaryListFileWriterTest", "a").toFile();
        this.writer = new BinaryListFileWriter(file);
        this.reader = new BinaryListFileReader(file);
        this.byAddressFileReader = new ByAddressFileReader(file);
    }

    public void tearDown() throws IOException {
        writer.close();
        reader.close();
        byAddressFileReader.close();
    }

    @Test
    public void shouldReadSingleList() throws IOException {
        assertNull(reader.read());

        BinaryList.Out bytesOut = new BinaryList.Out(5, new BufferBinaryOutput(new UnsafeBuffer(buffer)));

        bytesOut.add(out -> out.write(1));
        bytesOut.add(out -> out.write(2));
        bytesOut.add(out -> out.write(3));

        writer.write(bytesOut);

        BinaryList.In bytesIn = reader.read();

        assertEquals(3, bytesIn.size());

        AddressableItemIterator<BinaryInput> iterator = bytesIn.iterator();

        assertTrue(iterator.hasNext());
        BinaryInput next = iterator.next();
        assertEquals(1, next.readInt());
    }

    @Test
    public void shouldReadMultipleLists() throws IOException {
        BinaryList.Out bytesOut1 = new BinaryList.Out(5, new BufferBinaryOutput(new UnsafeBuffer(buffer)));
        bytesOut1.add(out -> out.write(1));
        bytesOut1.add(out -> out.write(2));
        writer.write(bytesOut1);

        BinaryList.Out bytesOut2 = new BinaryList.Out(5, new BufferBinaryOutput(new UnsafeBuffer(buffer)));
        bytesOut2.add(out -> out.write(2));
        bytesOut2.add(out -> out.write(3));
        bytesOut2.add(out -> out.write(4));
        writer.write(bytesOut2);

        BinaryList.In bytesIn = reader.read();
        assertEquals(2, bytesIn.size());

        bytesIn = reader.read();
        assertEquals(3, bytesIn.size());
    }

    @Test
    public void shouldAllowToNavigateToArbitraryListInFile() throws IOException {
        BinaryList.Out bytesOut1 = new BinaryList.Out(5, new BufferBinaryOutput(new UnsafeBuffer(buffer)));
        bytesOut1.add(out -> out.write(4356274L));
        bytesOut1.add(out -> out.write(7643565L));
        bytesOut1.add(out -> out.write(9874534L));
        writer.write(bytesOut1);

        BinaryList.Out bytesOut2 = new BinaryList.Out(5, new BufferBinaryOutput(new UnsafeBuffer(buffer)));
        bytesOut2.add(out -> out.write(5489234L));
        bytesOut2.add(out -> out.write(6903234L));
        bytesOut2.add(out -> out.write(8983434L));
        writer.write(bytesOut2);

        BinaryListWithAddress list1 = reader.readWithAddress();

        AddressableItemIterator<BinaryInput> it = list1.getBytes().iterator();
        it.next();
        it.next();
        long addr = list1.getAddress() + it.address();
        byte[] bytes = byAddressFileReader.readBytes(addr, 1024);
        BufferBinaryInput input = new BufferBinaryInput(new UnsafeBuffer(bytes));
        assertEquals(7643565L, input.readLong());

        BinaryListWithAddress list2 = reader.readWithAddress();
        BinaryList.In bytes2 = list2.getBytes();
        it = bytes2.iterator();
        it.next();
        it.next();
        addr = list2.getAddress() + it.address();
        bytes = byAddressFileReader.readBytes(addr, 1024);
        input = new BufferBinaryInput(new UnsafeBuffer(bytes));
        assertEquals(6903234L, input.readLong());
    }
}