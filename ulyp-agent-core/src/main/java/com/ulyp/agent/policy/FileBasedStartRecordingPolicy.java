package com.ulyp.agent.policy;

import com.ulyp.core.util.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * File based start recording policy. The file has the value stored which could be either 0 or 1.
 * Initially it has value 0, which means no recording can start at the moment. A user may write 1
 * to file
 */
@Slf4j
public class FileBasedStartRecordingPolicy implements StartRecordingPolicy {

    private static final Duration FILE_POLL_INTERVAL = Duration.ofSeconds(1);
    private static final char RECORDING_ENABLED_VALUE = '1';
    private static final char RECORDING_DISABLED_VALUE = '0';

    private final Path file;
    private volatile boolean recordingCanStart = false;

    public FileBasedStartRecordingPolicy(Path file) {
        this.file = file;
        if (!file.toFile().exists()) {
            writeToFile('0');
        }
        checkInitialValue();

        // No need to close
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
                NamedThreadFactory.builder()
                        .name("Ulyp-file-" + file + "-monitor")
                        .daemon(true)
                        .build()
        );
        scheduledExecutorService.scheduleAtFixedRate(
                this::checkFileContent,
                0,
                FILE_POLL_INTERVAL.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    private void writeToFile(char value) {
        try (FileOutputStream outputStream = new FileOutputStream(file.toFile())) {
            outputStream.write(value);
        } catch (IOException e) {
            throw new RuntimeException("Could not write to file " + file, e);
        }
    }

    private void checkInitialValue() {
        try (FileInputStream fileInputStream = new FileInputStream(file.toFile())) {
            byte[] buf = new byte[1];

            int bytesRead = fileInputStream.read(buf);

            if (bytesRead == 1) {
                char charValue = new String(buf, StandardCharsets.US_ASCII).charAt(0);
                boolean canStartRecordingValue = charValue == RECORDING_ENABLED_VALUE;
                if (canStartRecordingValue) {
                    log.info("Initial value in file {} is {}, recording can start any moment", file, charValue);
                    this.recordingCanStart = true;
                } else {
                    log.info("Initial value in file " + file + " is " + charValue + ", recording is disabled until content of file is changed");
                    this.recordingCanStart = false;
                }
            } else {
                throw new RuntimeException("Could not read one byte from file " + file);
            }
        } catch (IOException ioe) {
            // ignored
            log.debug("Error while checking file {}", file, ioe);
        }
    }

    private void checkFileContent() {
        try (FileInputStream fileInputStream = new FileInputStream(file.toFile())) {
            byte[] buf = new byte[1];

            int bytesRead = fileInputStream.read(buf);

            if (bytesRead == 1) {
                boolean canStartRecordingValue = new String(buf, StandardCharsets.US_ASCII).charAt(0) == RECORDING_ENABLED_VALUE;
                tryUpdateFlag(canStartRecordingValue);
            }
        } catch (IOException ioe) {
            // ignored
            log.debug("Error while checking file {}", file, ioe);
        }
    }

    private void tryUpdateFlag(boolean canStartRecordingValue) {
        if (!this.canStartRecording() && canStartRecordingValue) {
            log.info("Recording is enabled since value in file {} is 1", this.file);
            this.recordingCanStart = true;
        }
        if (this.canStartRecording() && !canStartRecordingValue) {
            log.info("Recording is disabled since value in file {} is 0", this.file);
            this.recordingCanStart = false;
        }
    }

    @Override
    public boolean canStartRecording() {
        return recordingCanStart;
    }

    @Override
    public String toString() {
        return "Can start recording any time";
    }
}
