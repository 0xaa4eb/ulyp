package com.ulyp.storage.impl;

import com.ulyp.core.Type;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.ReaderSettings;
import com.ulyp.storage.RecordingDataWriter;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsyncRecordingDataWriterTest {

    private AsyncFileRecordingDataReader reader;
    private RecordingDataWriter writer;

    @Before
    public void setUp() throws IOException {
        File file = Files.createTempFile(AsyncRecordingDataWriterTest.class.getSimpleName(), "a").toFile();
        this.reader = new AsyncFileRecordingDataReader(ReaderSettings.builder().file(file).autoStartReading(false).build());
        this.writer = new AsyncFileRecordingDataWriter(new FileRecordingDataWriter(file));
    }

    @After
    public void tearDown() {
        reader.close();
    }

    @Test
    public void shouldWriteAllTypesAfterCloseMethodCalled() {
        reader.start();

        List<TypeList> lists = new ArrayList<>();

        int typeListsCount = 10;
        int typesPerListCount = 100000;
        int id = 0;

        for (int i = 0; i < typeListsCount; i++) {
            TypeList list = new TypeList();
            for (int k = 0; k < typesPerListCount; k++) {
                list.add(
                        Type.builder()
                                .id(id++)
                                .name("a.b.C")
                                .typeTraits(Collections.emptySet())
                                .build()
                );
            }
            lists.add(list);
        }

        lists.forEach(writer::write);

        writer.close();

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(
                        () -> {

                            Assert.assertEquals(typeListsCount * typesPerListCount, reader.getTypes().values().size());
                        }
                );
    }

    public static class T {
        public String foo(String in) {
            return in;
        }
    }
}