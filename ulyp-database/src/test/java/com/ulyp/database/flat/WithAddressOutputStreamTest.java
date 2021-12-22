package com.ulyp.database.flat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Ignore
public class WithAddressOutputStreamTest {

    private WithAddressOutputStream writer;
    private FlatFileReader reader;

    @Before
    public void setUp() throws IOException {
        File file = Files.createTempFile(WithAddressOutputStreamTest.class.getSimpleName(), "b").toFile();

        writer = new WithAddressOutputStream(file);
        reader = new FlatFileReader(file);
    }

    @Test
    public void testWritingAndReadingBinaryList() throws IOException {
        writer.writeLong(534535L);
        writer.writeLong(-424234L);
        writer.writeLong(77777L);

        Assert.assertEquals(534535L, reader.readLong());
        Assert.assertEquals(-424234L, reader.readLong());
        Assert.assertEquals(77777L, reader.readLong());
    }
}