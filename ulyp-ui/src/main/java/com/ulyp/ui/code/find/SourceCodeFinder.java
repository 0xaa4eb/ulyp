package com.ulyp.ui.code.find;

import com.ulyp.ui.code.SourceCode;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SourceCodeFinder {

    private final List<JarFile> jars;
    private final ExecutorService decompilingExecutorService = Executors.newFixedThreadPool(2);

    public SourceCodeFinder(List<String> classpath) {

        this.jars = classpath.stream()
                .filter(x -> new File(x).exists())
                .filter(x -> new File(x).getName().endsWith(".jar"))
                .filter(x -> !new File(x).isDirectory())
                .map(x -> {
                    try {
                        return new JarFile(Paths.get(x).toFile());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<JarFile> sourcesJars = new ArrayList<>();

        for (JarFile jarFile : this.jars) {
            try {
                JarFile sourcesJar = jarFile.deriveSourcesJar();
                if (sourcesJar != null) {
                    sourcesJars.add(sourcesJar);
                }
            } catch (Exception e) {
                System.out.println("Could not open derive sources jar for " + jarFile);
            }
        }

        this.jars.addAll(0, sourcesJars);
    }

    public CompletableFuture<SourceCode> find(String javaClassName) {
        for (JarFile jar : this.jars) {
            SourceCode sourceCode = jar.findSourceByClassName(javaClassName);

            if (sourceCode != null) {
                return CompletableFuture.completedFuture(sourceCode);
            }
        }

        CompletableFuture<SourceCode> result = new CompletableFuture<>();

        decompilingExecutorService.execute(
                () -> {
                    for (JarFile jar : this.jars) {
                        ByteCode byteCode = jar.findByteCodeByClassName(javaClassName);

                        if (byteCode != null) {
                            result.complete(byteCode.decompile().prependToSource(String.format("// Decompiled from: %s \n", jar.getAbsolutePath())));
                        }
                    }

                    result.complete(new SourceCode("", ""));
                }
        );

        return result;
    }
//    static class ForFolder implements ClassFileLocator {
//
//        private final File folder;
//
//        public ForFolder(File folder) {
//            this.folder = folder;
//        }
//
//        public Resolution locate(String name) throws IOException {
//            File file = new File(folder, name.replace('.', File.separatorChar) + CLASS_FILE_EXTENSION);
//            if (file.exists()) {
//                InputStream inputStream = new FileInputStream(file);
//                try {
//                    return new Resolution.Explicit(StreamDrainer.DEFAULT.drain(inputStream));
//                } finally {
//                    inputStream.close();
//                }
//            } else {
//                return new Resolution.Illegal(name);
//            }
//        }
//
//        public void close() {
//            /* do nothing */
//        }
//    }
}
