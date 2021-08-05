package com.ulyp.ui.code.find;

import java.io.File;
import java.nio.file.Path;

public class GradleJarFile extends JarFile {

    protected GradleJarFile(File path) {
        super(path);
    }

    protected static boolean isGradleJarFile(Path path) {
        return path.toString().contains(".gradle");
    }
}
