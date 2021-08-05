package com.ulyp.ui.code.find;

import com.ulyp.ui.code.SourceCode;
import com.ulyp.ui.util.file.TmpFile;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ByteCode {

    private final String className;
    private final byte[] bytecode;

    public ByteCode(String className, byte[] bytecode) {
        this.className = className;
        this.bytecode = bytecode;
    }

    SourceCode decompile() {
        TmpFile classfile = new TmpFile(className + ".class");

        try (FileOutputStream fileOutputStream = new FileOutputStream(classfile.getPath().toFile())) {
            fileOutputStream.write(bytecode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ConsoleDecompiler.main(
                new String[] {
                        classfile.getPath().toAbsolutePath().toString(),
                        classfile.getPath().getParent().toAbsolutePath().toString()
                });

        Path output = Paths.get(classfile.getPath().getParent().toAbsolutePath().toString(), className + ".java");
        try {
            return new SourceCode(className, Files.readAllLines(output).stream().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            output.toFile().delete();
        }
    }
}
