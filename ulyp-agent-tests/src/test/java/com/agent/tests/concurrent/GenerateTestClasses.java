package com.agent.tests.concurrent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

        for (int i = 0; i <= 100; i++) {
            String name = "X" + i;

            try(BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(pathStr, name + ".java").toFile()))) {
                writer.append("package ").append(pckg).append(";");
                writer.newLine();
                writer.append("// Automatically generated and only used for tests");
                writer.newLine();
                writer.append("@SuppressWarnings(\"unused\")");
                writer.newLine();
                writer.append("public class ").append(name).append(" {");
                writer.newLine();
                appendStringArgsMethod(writer, "m1");
                appendStringArgsMethod(writer, "m2");
                appendStringArgsMethod(writer, "m3");
                appendStringArgsMethod(writer, "m4");
                appendStringArgsMethod(writer, "m5");
                appendIntArgsMethod(writer, "m8");
                appendIntArgsMethod(writer, "m9");
                appendIntArgsMethod(writer, "m10");
                appendIntArgsMethod(writer, "m11");
                appendIntArgsMethod(writer, "m12");
                appendSimpleMethod(writer, "m13");
                appendSimpleMethod(writer, "m14");
                appendSimpleMethod(writer, "m15");
                appendSimpleMethod(writer, "m6");
                appendSimpleMethod(writer, "m7");
                writer.append("}");
                writer.newLine();
            }
        }
    }

    private static void appendSimpleMethod(BufferedWriter writer, String methodName) throws IOException {
        writer.append("\tpublic void ").append(methodName).append("() {");
        writer.newLine();
        writer.append("\t\tSystem.out.println(\"1\");");
        writer.newLine();
        writer.append("\t}");
        writer.newLine();
    }

    private static void appendStringArgsMethod(BufferedWriter writer, String methodName) throws IOException {
        writer.append("\tpublic String ").append(methodName).append("(String a, String b, String c) {");
        writer.newLine();
        writer.append("\t\tSystem.out.println(\"1\");");
        writer.newLine();
        writer.append("\t\treturn a + b + c;");
        writer.newLine();
        writer.append("\t}");
        writer.newLine();
    }

    private static void appendIntArgsMethod(BufferedWriter writer, String methodName) throws IOException {
        writer.append("\tpublic String ").append(methodName).append("(Integer x, Long y, Long z) {");
        writer.newLine();
        writer.append("\t\tSystem.out.println(\"1\");");
        writer.newLine();
        writer.append("\t\treturn String.valueOf(y + z);");
        writer.newLine();
        writer.append("\t}");
        writer.newLine();
    }
}
