package com.ulyp.storage.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.ulyp.core.Type;
import com.ulyp.core.mem.SerializedTypeList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.storage.writer.RecordingDataWriter;
import com.ulyp.storage.writer.FileRecordingDataWriter;

public class AsyncRecordingDataWriterTest {

    private FileRecordingDataReader reader;
    private RecordingDataWriter writer;

    @Before
    public void setUp() throws IOException {
        File file = Files.createTempFile(AsyncRecordingDataWriterTest.class.getSimpleName(), "a").toFile();
        this.reader = new FileRecordingDataReaderBuilder(file).build();
        this.writer = new FileRecordingDataWriter(file);
    }

    @After
    public void tearDown() {
        reader.close();
    }

    @Test
    public void shouldReturnNullProcessMetadataIfNotWritten() {
        Assert.assertNull(reader.getProcessMetadata());
    }

    @Test
    public void shouldReturnNullIfProcessMetadataIsNotWrittenFirst() {
        writer.write(
            RecordingMetadata.builder()
                .id(1)
                .threadId(2)
                .logCreatedEpochMillis(999L)
                .recordingStartedEpochMillis(90)
                .recordingCompletedEpochMillis(100)
                .build()
        );
        writer.write(
            ProcessMetadata.builder()
                .pid(5435L)
                .mainClassName("a.b.c.D")
                .build()
        );

        Assert.assertNull(reader.getProcessMetadata());
    }

    @Test
    public void shouldReturnProcessMetadataIfWrittenFirst() {
        writer.write(
            ProcessMetadata.builder()
                .pid(5435L)
                .mainClassName("a.b.c.D")
                .build()
        );

        SerializedTypeList types = new SerializedTypeList();
        types.add(Type.builder().name("a.b.Type").build());
        writer.write(types);

        ProcessMetadata processMetadata = reader.getProcessMetadata();

        Assert.assertEquals("a.b.c.D", processMetadata.getMainClassName());
        Assert.assertEquals(5435L, processMetadata.getPid());
    }
}
