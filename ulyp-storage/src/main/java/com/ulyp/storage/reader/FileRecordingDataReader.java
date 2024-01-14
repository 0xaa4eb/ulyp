package com.ulyp.storage.reader;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.ulyp.core.*;
import com.ulyp.core.mem.*;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.repository.ReadableRepository;
import com.ulyp.core.serializers.MethodSerializer;
import com.ulyp.core.serializers.ProcessMetadataSerializer;
import com.ulyp.core.serializers.RecordingMetadataSerializer;

import com.ulyp.core.serializers.TypeSerializer;
import com.ulyp.core.util.NamedThreadFactory;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.util.BinaryListFileReader;

import lombok.SneakyThrows;

public class FileRecordingDataReader implements RecordingDataReader {

    private final File file;
    private final RecordedMethodCallDataReader recordedMethodCallDataReader;
    private final ExecutorService executorService;
    private boolean closed = false;

    FileRecordingDataReader(File file, int threads) {
        this.file = file;
        this.recordedMethodCallDataReader = new RecordedMethodCallDataReader(file);
        this.executorService = Executors.newFixedThreadPool(
            threads,
            NamedThreadFactory.builder()
                .name("Reader-" + file.toString())
                .daemon(true)
                .build()
        );
    }

    @Override
    public CompletableFuture<Void> submitReaderJob(RecordingDataReaderJob job) {
        try {
            return CompletableFuture.runAsync(new JobRunner(job, file), executorService);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RecordedEnterMethodCall readEnterMethodCall(long address, ReadableRepository<Integer, Type> typeRepository) {
        return recordedMethodCallDataReader.readEnterMethodCall(address, typeRepository);
    }

    @Override
    public RecordedExitMethodCall readExitMethodCall(long address, ReadableRepository<Integer, Type> typeRepository) {
        return recordedMethodCallDataReader.readExitMethodCall(address, typeRepository);
    }

    @Override
    public ProcessMetadata getProcessMetadata() {
        try (BinaryListFileReader reader = new BinaryListFileReader(file)) {
            BinaryListWithAddress binaryListWithAddress = reader.readWithAddress();
            if (binaryListWithAddress == null) {
                return null;
            }
            ReadBinaryList bytes = binaryListWithAddress.getBytes();
            if (bytes.id() != ProcessMetadata.WIRE_ID) {
                return null;
            }
            return deserializeProcessMetadata(bytes);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void close() throws StorageException {
        if (!closed) {
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            try {
                recordedMethodCallDataReader.close();
            } catch (IOException e) {
                throw new StorageException(e);
            }
        }
    }

    private static class JobRunner implements Runnable {

        private final File file;
        private final RecordingDataReaderJob job;

        private JobRunner(RecordingDataReaderJob job, File file) throws IOException {
            this.file = file;
            this.job = job;
        }

        @SneakyThrows
        @Override
        public void run() {
            try (BinaryListFileReader reader = new BinaryListFileReader(file)) {
                while (!Thread.currentThread().isInterrupted()) {

                    try {
                        BinaryListWithAddress data = reader.readWithAddress();

                        if (data == null) {
                            if (job.continueOnNoData()) {
                                continue;
                            } else {
                                return;
                            }
                        }

                        switch (data.getBytes().id()) {
                            case ProcessMetadata.WIRE_ID:
                                onProcessMetadata(data.getBytes());
                                break;
                            case RecordingMetadata.WIRE_ID:
                                onRecordingMetadata(data.getBytes());
                                break;
                            case TypeList.WIRE_ID:
                                onTypes(data.getBytes());
                                break;
                            case MethodList.WIRE_ID:
                                onMethods(data.getBytes());
                                break;
                            case RecordedMethodCallList.WIRE_ID:
                                onRecordedCalls(data);
                                break;
                            case RecordingCompleteMark.WIRE_ID:
                                return;
                            default:
                                throw new StorageException("Unknown binary data id " + data.getBytes().id());
                        }
                    } catch (Exception err) {
                        throw new RuntimeException(err);
                    }
                }
            }
        }

        private void onProcessMetadata(ReadBinaryList data) {
            job.onProcessMetadata(FileRecordingDataReader.deserializeProcessMetadata(data));
        }

        protected void onRecordingMetadata(ReadBinaryList readBinaryList) {
            job.onRecordingMetadata(RecordingMetadataSerializer.instance.deserialize(readBinaryList.iterator().next()));
        }

        private void onTypes(ReadBinaryList typesList) {
            for (BinaryInput input : typesList) {
                job.onType(TypeSerializer.instance.deserialize(input));
            }
        }

        private void onMethods(ReadBinaryList methodList) {
            for (BinaryInput input : methodList) {
                job.onMethod(MethodSerializer.instance.deserialize(input));
            }
        }

        private void onRecordedCalls(BinaryListWithAddress data) {
            RecordedMethodCallList calls = new RecordedMethodCallList(data.getBytes());
            job.onRecordedCalls(data.getAddress(), calls);
        }
    }

    private static ProcessMetadata deserializeProcessMetadata(ReadBinaryList readBinaryList) {
        return ProcessMetadataSerializer.instance.deserialize(readBinaryList.iterator().next());
    }
}
