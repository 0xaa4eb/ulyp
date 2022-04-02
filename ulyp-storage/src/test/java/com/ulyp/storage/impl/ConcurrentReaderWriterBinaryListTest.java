package com.ulyp.storage.impl;

import com.ulyp.core.mem.BinaryList;
import com.ulyp.storage.impl.util.BinaryListFileReader;
import com.ulyp.storage.impl.util.BinaryListFileWriter;
import lombok.Builder;
import lombok.Value;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.*;

public class ConcurrentReaderWriterBinaryListTest {

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private File file;

    @Before
    public void setUp() throws IOException {
        file = Files.createTempFile(ConcurrentReaderWriterBinaryListTest.class.getSimpleName(), "a").toFile();
    }

    @After
    public void tearDown() throws InterruptedException {
        executorService.shutdownNow();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Value
    @Builder
    public static class ReadResult {
        int itemsCount;
    }

    public static class Reader implements Callable<ReadResult> {

        private final BinaryListFileReader reader;
        private final int expectedListsCount;

        public Reader(File file, int expectedListsCount) throws IOException {
            this.reader = new BinaryListFileReader(file);
            this.expectedListsCount = expectedListsCount;
        }

        @Override
        public ReadResult call() throws Exception {
            int itemsRead = 0;

            for (int i = 0; i < expectedListsCount; ) {
                BinaryList binaryList = reader.read();

                if (binaryList != null) {
                    itemsRead += binaryList.size();
                    i++;
                }
            }
            return ReadResult.builder().itemsCount(itemsRead).build();
        }
    }

    @Builder
    @Value
    private static class WriteResult {
        int itemsWritten;
    }

    public static class Writer implements Callable<WriteResult> {

        private final BinaryListFileWriter writer;
        private final int listsToWrite;
        private final Duration maxSleep;

        public Writer(File file, int listsToWrite, Duration maxSleep) throws IOException {
            this.writer = new BinaryListFileWriter(file);
            this.listsToWrite = listsToWrite;
            this.maxSleep = maxSleep;
        }

        @Override
        public WriteResult call() throws Exception {
            int itemsWritten = 0;

            for (int i = 0; i < listsToWrite; i++) {
                BinaryList list = new BinaryList(0);

                for (int k = 0; k < ThreadLocalRandom.current().nextInt(10); k++) {
                    list.add("ABC".getBytes());
                    itemsWritten++;
                }
                writer.append(list);

                if (!maxSleep.isZero()) {
                    Thread.sleep(ThreadLocalRandom.current().nextLong(maxSleep.toMillis()));
                }
            }

            return WriteResult.builder().itemsWritten(itemsWritten).build();
        }
    }

    @Test
    public void shouldBeAbleToConcurrentlyWriteAndRead() throws Exception {
        verify(500, Duration.ofMillis(0));

        verify(500, Duration.ofMillis(1));

        verify(500, Duration.ofMillis(10));
    }

    private void verify(int listsCount, Duration maxSleep) throws Exception {
        Future<WriteResult> writeResultFuture = executorService.submit(new Writer(file, listsCount, maxSleep));
        Future<ReadResult> readResultFuture = executorService.submit(new Reader(file, listsCount));

        WriteResult writeResult = writeResultFuture.get(10, TimeUnit.SECONDS);
        ReadResult readResult = readResultFuture.get(10, TimeUnit.SECONDS);

        Assert.assertEquals(writeResult.getItemsWritten(), readResult.getItemsCount());
    }
}