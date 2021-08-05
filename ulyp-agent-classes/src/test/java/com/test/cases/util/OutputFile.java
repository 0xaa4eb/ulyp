package com.test.cases.util;

import com.ulyp.transport.TCallRecordLogUploadRequest;
import org.junit.Assert;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class OutputFile {

    private final Path file;

    public OutputFile(String prefix, String suffix) {
        try {
            this.file = Files.createTempFile(prefix, suffix);
            file.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TCallRecordLogUploadRequest> read() {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file.toFile()))) {

            List<TCallRecordLogUploadRequest> result = new ArrayList<>();

            while (inputStream.available() > 0) {
                result.add(TCallRecordLogUploadRequest.parseDelimitedFrom(inputStream));
            }

            return result;
        } catch (Exception e) {
            Assert.fail("Failed to parse " + file);
            throw new RuntimeException();
        }
    }

    @Override
    public String toString() {
        return "" + file;
    }
}
