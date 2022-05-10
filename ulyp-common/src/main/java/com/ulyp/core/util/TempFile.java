package com.ulyp.core.util;

import com.ulyp.core.exception.UlypException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TempFile {

    private final Path path;

    public TempFile() {
        this("ulyp", ".dat");
    }

    public TempFile(String prefix, String suffix) {
        try {
            this.path = Files.createTempFile(prefix, suffix);
            this.path.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new UlypException("Error while creating temp file with " +
                    "prefix='" + prefix + "' and suffix='" + suffix + "'", e);
        }
    }

    public Path toPath() {
        return path;
    }

    @Override
    public String toString() {
        return "TempFile{" +
                "path=" + path +
                '}';
    }
}
