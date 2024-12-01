package com.ulyp.storage.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.ulyp.core.Type;
import com.ulyp.core.mem.SerializedTypeList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.AfterEach;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.storage.writer.RecordingDataWriter;
import com.ulyp.storage.writer.FileRecordingDataWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AsyncRecordingDataWriterTest {

    private FileRecordingDataReader reader;
    private RecordingDataWriter writer;

    @BeforeEach
    public void setUp() throws IOException {
        File file = Files.createTempFile(AsyncRecordingDataWriterTest.class.getSimpleName(), "a").toFile();
        this.reader = new FileRecordingDataReaderBuilder(file).build();
        this.writer = new FileRecordingDataWriter(file);
    }

    @AfterEach
    public void tearDown() {
        reader.close();
    }

    @Test
    void shouldReturnNullProcessMetadataIfNotWritten() {
        assertNull(reader.getProcessMetadata());
    }

    @Test
    void shouldReturnNullIfProcessMetadataIsNotWrittenFirst() {
        writer.write(
            RecordingMetadata.builder()
                .id(1)
                .threadId(2)
                .recordingStartedMillis(90)
                .recordingFinishedMillis(100)
                .build()
        );
        writer.write(
            ProcessMetadata.builder()
                .pid(5435L)
                .mainClassName("a.b.c.D")
                .build()
        );

        assertNull(reader.getProcessMetadata());
    }

    @Test
    void shouldReturnProcessMetadataIfWrittenFirst() {
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

        assertEquals("a.b.c.D", processMetadata.getMainClassName());
        assertEquals(5435L, processMetadata.getPid());
    }
}
