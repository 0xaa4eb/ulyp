package com.ulyp.storage.impl;

import com.ulyp.core.RecordingMetadata;
import com.ulyp.storage.StorageReader;
import com.ulyp.storage.StorageWriter;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

import static org.junit.Assert.*;

public class StorageReadWriteTest {

    private StorageReader reader;
    private StorageWriter writer;

    @Before
    public void setUp() throws IOException {
        File file = Files.createTempFile(StorageReadWriteTest.class.getSimpleName(), "a").toFile();
        this.reader = new StorageReaderImpl(file);
        this.writer = new StorageWriterImpl(file);
    }

    @After
    public void tearDown() {
        reader.close();
        writer.close();
    }

    @Test
    public void testReadWriteRecordingWithNoData() throws IOException {

        writer.store(
                RecordingMetadata.builder()
                        .id(42)
                        .createEpochMillis(2324L)
                        .threadName("AAAA")
                        .threadId(4343L)
                        .build()
        );

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(
                        () -> {
                            Assert.assertEquals(1, reader.availableRecordings().size());
                        }
                );
    }
}