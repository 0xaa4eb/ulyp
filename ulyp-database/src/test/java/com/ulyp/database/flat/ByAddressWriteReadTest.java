package com.ulyp.database.flat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ByAddressWriteReadTest {

    private ByAddressFileWriter writer;
    private ByAddressFileReader reader;

    @Before
    public void setUp() throws IOException {
        File file = Files.createTempFile("FlatFileWriterTest", "a").toFile();
        this.writer = new ByAddressFileWriter(file);
        this.reader = new ByAddressFileReader(file);
    }

    @Test
    public void testWritingLongAtAddress() throws IOException {

        writer.writeAt(50, 5423423L);

        Assert.assertEquals(5423423L, reader.readLongAt(50));

        writer.writeAt(1024, Long.MIN_VALUE + 99);

        Assert.assertEquals(Long.MIN_VALUE + 99, reader.readLongAt(1024));
    }
}