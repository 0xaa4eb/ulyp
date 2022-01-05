package com.ulyp.database.flat;

import com.ulyp.storage.impl.ByAddressFileReader;
import com.ulyp.storage.impl.ByAddressFileWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Ignore
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

        writer.writeAt(50, (byte) 0);

        Assert.assertEquals(0, reader.readByte(50));

        writer.writeAt(1024, (byte) 13);

        Assert.assertEquals(13, reader.readByte(1024));
    }
}