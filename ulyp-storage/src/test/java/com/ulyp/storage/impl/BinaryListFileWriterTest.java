package com.ulyp.storage.impl;

import com.ulyp.core.mem.BinaryList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

public class BinaryListFileWriterTest {

    private BinaryListFileWriter writer;
    private BinaryListFileReader reader;

    @Before
    public void setUp() throws IOException {
        File file = Files.createTempFile("BinaryListFileWriterTest", "a").toFile();
        this.writer = new BinaryListFileWriter(file);
        this.reader = new BinaryListFileReader(file);
    }

    @Test
    public void shouldReturnNullWhenNoListIsWritten() throws IOException, InterruptedException {
        Assert.assertNull(reader.read(Duration.ofSeconds(1)));

    }

    @Test
    public void shouldReadSingleList() throws IOException, InterruptedException {
        BinaryList list1 = BinaryList.of(42, "ABC".getBytes(), "DEFG".getBytes());

        writer.append(list1);

        BinaryList binaryList = reader.read(Duration.ofSeconds(5));

        Assert.assertEquals(list1, binaryList);
        Assert.assertNull(reader.read(Duration.ofSeconds(1)));
    }

    @Test
    public void shouldReadMultipleLists() throws IOException, InterruptedException {
        BinaryList list1 = BinaryList.of(42, "ABC".getBytes(), "DEFG".getBytes());
        BinaryList list2 = BinaryList.of(777, "ZFD".getBytes());

        writer.append(list1);
        writer.append(list2);

        Assert.assertEquals(list1, reader.read(Duration.ofSeconds(5)));
        Assert.assertEquals(list2, reader.read(Duration.ofSeconds(5)));
        Assert.assertNull(reader.read(Duration.ofSeconds(1)));
    }

    @Test
    public void shouldReadMultipleListsOneAfterAnother() throws IOException, InterruptedException {
        BinaryList list1 = BinaryList.of(42, "ABC".getBytes(), "DEFG".getBytes());
        writer.append(list1);

        Assert.assertEquals(list1, reader.read(Duration.ofSeconds(5)));

        BinaryList list2 = BinaryList.of(777, "ZFD".getBytes());
        writer.append(list2);

        Assert.assertEquals(list2, reader.read(Duration.ofSeconds(5)));

        Assert.assertNull(reader.read(Duration.ofSeconds(1)));
    }
}