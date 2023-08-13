package com.ulyp.core.util;

import com.ulyp.core.exception.UlypException;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

public class TempDirectory {

    private final Path path;

    public TempDirectory() {
        this("ulyp");
    }

    public TempDirectory(String prefix) {
        try {
            this.path = Files.createTempDirectory(prefix);
            DeleteOnExitHook.add(this.path.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new UlypException("Error while creating temp file with prefix='" + prefix + "'", e);
        }
    }

    public Path toPath() {
        return path;
    }

    @Override
    public String toString() {
        return "TempDirectory{" +
                "path=" + path +
                '}';
    }

    private static class DeleteOnExitHook {

        private static LinkedHashSet<String> files = new LinkedHashSet<>();

        static {
            Runtime.getRuntime().addShutdownHook(new Thread(DeleteOnExitHook::runHooks));
        }

        private DeleteOnExitHook() {}

        static synchronized void add(String file) {
            if(files == null) {
                // DeleteOnExitHook is running. Too late to add a file
                throw new IllegalStateException("Shutdown in progress");
            }

            files.add(file);
        }

        static void runHooks() {
            LinkedHashSet<String> theFiles;

            synchronized (DeleteOnExitHook.class) {
                theFiles = files;
                files = null;
            }

            ArrayList<String> toBeDeleted = new ArrayList<>(theFiles);

            Collections.reverse(toBeDeleted);
            for (String filename : toBeDeleted) {
                delete(filename);
            }
        }

        // for Ulyp purposes only directory with files deletion is needed, no need to support deletion of subdirectories
        private static void delete(String filename) {
            try {
                File file = new File(filename);
                if (!file.exists()) {
                    return;
                }
                Files.walkFileTree(file.toPath(), new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        file.toFile().delete(); // temp file - ignore result
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                // temp file - ignore result
            }
        }
    }
}
