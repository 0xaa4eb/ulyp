package com.ulyp.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

class TempFileTest {

    @Test
    void shouldCreateTempFileWithDefaultPrefixAndSuffix() {
        TempFile tempFile = new TempFile();
        
        Path path = tempFile.toPath();
        String fileName = path.getFileName().toString();
        
        assertTrue(Files.exists(path), 
            "Temporary file should exist at path: " + path);
        assertTrue(fileName.startsWith("ulyp"), 
            "File name should start with 'ulyp' but was: " + fileName);
        assertTrue(fileName.endsWith(".dat"), 
            "File name should end with '.dat' but was: " + fileName);
    }

    @Test
    void shouldCreateTempFileWithCustomPrefixAndSuffix() {
        TempFile tempFile = new TempFile("custom", ".txt");
        
        Path path = tempFile.toPath();
        String fileName = path.getFileName().toString();
        
        assertTrue(Files.exists(path), 
            "Temporary file should exist at path: " + path);
        assertTrue(fileName.startsWith("custom"), 
            "File name should start with 'custom' but was: " + fileName);
        assertTrue(fileName.endsWith(".txt"), 
            "File name should end with '.txt' but was: " + fileName);
    }

    @Test
    void toStringShouldContainPath() {
        TempFile tempFile = new TempFile();
        
        String toString = tempFile.toString();
        
        assertTrue(toString.contains("TempFile"), 
            "toString() should contain 'TempFile' but was: " + toString);
        assertTrue(toString.contains("path="), 
            "toString() should contain 'path=' but was: " + toString);
    }
} 