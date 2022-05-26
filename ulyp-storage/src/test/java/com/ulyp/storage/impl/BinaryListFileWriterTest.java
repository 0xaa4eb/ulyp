package com.ulyp.storage.impl;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.storage.impl.util.BinaryListFileReader;
import com.ulyp.storage.impl.util.BinaryListFileWriter;
import com.ulyp.storage.impl.util.ByAddressFileReader;
import com.ulyp.transport.BinaryDataDecoder;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class BinaryListFileWriterTest {

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

    @Test
    public void shouldReturnNullWhenNoListIsWritten() throws IOException, InterruptedException {
        Assert.assertNull(reader.read());

    }

    @Test
    public void shouldReadSingleList() throws IOException, InterruptedException {
        BinaryList list1 = BinaryList.of(42, "ABC".getBytes(), "DEFG".getBytes());

        writer.append(list1);

        BinaryList binaryList = reader.read();

        Assert.assertEquals(list1, binaryList);
        Assert.assertNull(reader.read());
    }

    @Test
    public void shouldReadMultipleLists() throws IOException, InterruptedException {
        BinaryList list1 = BinaryList.of(42, "ABC".getBytes(), "DEFG".getBytes());
        BinaryList list2 = BinaryList.of(777, "ZFD".getBytes());

        writer.append(list1);
        writer.append(list2);

        Assert.assertEquals(list1, reader.read());
        Assert.assertEquals(list2, reader.read());
        Assert.assertNull(reader.read());
    }

    @Test
    public void shouldReadMultipleListsOneAfterAnother() throws IOException, InterruptedException {
        BinaryList list1 = BinaryList.of(42, "ABC".getBytes(), "DEFG".getBytes());
        writer.append(list1);

        Assert.assertEquals(list1, reader.read());

        BinaryList list2 = BinaryList.of(777, "ZFD".getBytes());
        writer.append(list2);

        Assert.assertEquals(list2, reader.read());

        Assert.assertNull(reader.read());
    }

    @Test
    public void shouldAllowToNavigateToArbitraryListInFile() throws IOException {
        BinaryList list1 = BinaryList.of(42, "ABC".getBytes(), "DEFG".getBytes());
        writer.append(list1);

        BinaryList list2 = BinaryList.of(777, "ZFD".getBytes());
        writer.append(list2);

        BinaryListWithAddress data1 = reader.readWithAddress();
        byte[] list1Raw = byAddressFileReader.readBytes(data1.getAddress(), 8 * 1024);

        Assert.assertEquals(list1, new BinaryList(list1Raw));

        BinaryListWithAddress data2 = reader.readWithAddress();
        byte[] list2Raw = byAddressFileReader.readBytes(data2.getAddress(), 8 * 1024);

        Assert.assertEquals(list2, new BinaryList(list2Raw));
    }

    @Test
    public void shouldAllowToNavigateToAnyItemInAnyListInFile() throws IOException {
        BinaryList list1 = BinaryList.of(42, "ABC".getBytes(), "DEFG".getBytes());
        writer.append(list1);

        BinaryList list2 = BinaryList.of(777, "ZFD".getBytes());
        writer.append(list2);

        BinaryListWithAddress data1 = reader.readWithAddress();
        AddressableItemIterator<BinaryDataDecoder> iterator1 = data1.getBytes().iterator();
        iterator1.next();
        long addressOfItem1 = iterator1.address(); // within the list address
        System.out.println(addressOfItem1);
        BinaryDataDecoder decoder = new BinaryDataDecoder();
        decoder.wrap(
                new UnsafeBuffer(byAddressFileReader.readBytes(data1.getAddress() + addressOfItem1, 8 * 1024)),
                0,
                BinaryDataDecoder.BLOCK_LENGTH,
                0
        );

        System.out.println(decoder.valueLength());
    }
}