package com.ulyp.ui.code.find;

import com.ulyp.ui.code.SourceCode;
import com.ulyp.ui.util.StreamDrainer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;

public class JarFile {

    private static final String CLASS_FILE_EXTENSION = ".class";
    private static final String CLASS_SOURCE_FILE_EXTENSION = ".java";

    private final File file;
    private final java.util.jar.JarFile jarFile;

    protected JarFile(File file) {
        try {
            this.file = file;
            this.jarFile = new java.util.jar.JarFile(file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not find jar file: " + file, e);
        }
    }

    /**
     * For maven/gradle jar files it's possible to locate jar file with source code
     * @return jar file with source if possible to locate or null otherwise
     */
    @Nullable
    public JarFile deriveSourcesJar() {
        Path libFolder = file.toPath().getParent().getParent();

        for (File p : libFolder.toFile().listFiles()) {
            if (p.isDirectory()) {
                for (File jarFile : p.listFiles()) {
                    if (jarFile.getAbsolutePath().contains("-sources.jar")) {
                        return new JarFile(jarFile);
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    public SourceCode findSourceByClassName(String className) {
        ZipEntry zipEntry = jarFile.getEntry(className.replace('.', '/') + CLASS_SOURCE_FILE_EXTENSION);
        if (zipEntry == null) {
            return null;
        } else {
            try (InputStream inputStream = jarFile.getInputStream(zipEntry)) {
                return new SourceCode(className, new String(StreamDrainer.DEFAULT.drain(inputStream)));
            } catch (IOException e) {
                throw new RuntimeException("Could not read jar file: " + e.getMessage(), e);
            }
        }
    }

    @Nullable
    public ByteCode findByteCodeByClassName(String className) {
        ZipEntry zipEntry = jarFile.getEntry(className.replace('.', '/') + CLASS_FILE_EXTENSION);
        if (zipEntry == null) {
            return null;
        } else {
            try (InputStream inputStream = jarFile.getInputStream(zipEntry)) {
                return new ByteCode(className, StreamDrainer.DEFAULT.drain(inputStream));
            } catch (IOException e) {
                throw new RuntimeException("Could not read jar file: " + e.getMessage(), e);
            }
        }
    }

    public String getAbsolutePath() {
        return this.file.getAbsolutePath();
    }
}
