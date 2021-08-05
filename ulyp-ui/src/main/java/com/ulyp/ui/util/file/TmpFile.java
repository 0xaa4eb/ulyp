package com.ulyp.ui.util.file;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TmpFile {

    private final Path path;

    public TmpFile(String name) {
        this.path = Paths.get(System.getProperty("java.io.tmpdir"), name);

        new File(name).deleteOnExit();
    }

    public Path getPath() {
        return path;
    }
}
