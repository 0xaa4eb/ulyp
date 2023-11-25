package com.agent.tests.concurrent;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GenerateTestClasses {

    public static void main(String[] args) throws IOException {

        String pathStr = args[0];
        Path path = Paths.get(pathStr);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Path doesn't exists: " + pathStr);
        }
        String pckg = args[1];

        for (int i = 0; i <= 1000; i++) {
            String name = "X" + i;

            try(BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(pathStr, name + ".java").toFile()))) {
                writer.append("package ").append(pckg).append(";");
                writer.newLine();
                writer.append("// Automatically generated and only used for tests");
                writer.newLine();
                writer.append("public class ").append(name).append(" {");
                writer.newLine();
                appendMethod(writer, "foo");
                appendMethod(writer, "bar");
                appendMethod(writer, "xyz");
                appendMethod(writer, "qwe");
                appendMethod(writer, "asd");
                appendMethod(writer, "vcd");
                appendMethod(writer, "tyu");
                writer.append("}");
                writer.newLine();
            }
        }
    }

    private static void appendMethod(BufferedWriter writer, String methodName) throws IOException {
        writer.append("\tpublic void ").append(methodName).append("() {");
        writer.newLine();
        writer.append("\t\tSystem.out.println(\"1\");");
        writer.newLine();
        writer.append("\t}");
        writer.newLine();
    }
}
