package com.ulyp.agent.transport.file;

import com.ulyp.core.CallEnterRecordList;
import com.ulyp.core.CallExitRecordList;
import com.ulyp.transport.TCallRecordLogUploadRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FileWriterTask implements Runnable {

    private static final TCallRecordLogUploadRequest POISON_PILL = TCallRecordLogUploadRequest.newBuilder().build();
    private final BlockingQueue<TCallRecordLogUploadRequest> requestQueue = new LinkedBlockingQueue<>();
    private final Path filePath;
    private volatile boolean active = true;

    public FileWriterTask(Path filePath) {
        this.filePath = filePath;
    }

    void addToQueue(TCallRecordLogUploadRequest request) {
        requestQueue.add(request);
    }

    void shutdownAndWaitForTasksToComplete(long time, TimeUnit timeUnit) throws InterruptedException {
        log.info("Shutting down file writing...");
        requestQueue.add(POISON_PILL);

        long deadline = System.currentTimeMillis() + timeUnit.toMillis(time);

        while (System.currentTimeMillis() < deadline && active) {
            Thread.sleep(100);
        }

        log.info("Shut down file writing");
    }

    @Override
    public void run() {
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath.toFile(), false))) {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    TCallRecordLogUploadRequest request = requestQueue.poll(1, TimeUnit.SECONDS);
                    if (request != null) {
                        if (request == POISON_PILL) {
                            log.info("Got poison pill, won't write any chunks to file, queue stil got " + requestQueue.size() + " requests in it");
                            return;
                        } else {

                            write(outputStream, request);
                        }
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        } catch (IOException e) {
            // TODO ERROR log to console with ULYP logger
            System.err.println("Error while writing to file " + filePath);
            e.printStackTrace();
        } finally {
            active = false;
        }
    }

    private void write(OutputStream outputStream, TCallRecordLogUploadRequest request) throws IOException {
        log.info("Writing request: recording id = " + request.getRecordingInfo().getRecordingId() +
                ", enter records = " + new CallEnterRecordList(request.getRecordLog().getEnterRecords()).size() +
                ", exit records = " + new CallExitRecordList(request.getRecordLog().getExitRecords()).size());

        request.writeDelimitedTo(outputStream);
        outputStream.flush();
    }
}
