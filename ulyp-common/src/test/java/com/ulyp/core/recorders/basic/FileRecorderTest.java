package com.ulyp.core.recorders.basic;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileRecorderTest {

    private final FileRecorder recorder = new FileRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldSupportFileType() {
        assertTrue(recorder.supports(File.class));
        
        assertFalse(recorder.supports(Path.class));
        assertFalse(recorder.supports(String.class));
        assertFalse(recorder.supports(Object.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @Test
    void shouldRecordSimpleFile() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        File testValue = new File("test.txt");

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(File.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(FileRecord.class, record);
        assertEquals(testValue.getPath(), record.toString());
    }

    @Test
    void shouldRecordAbsoluteFile(@TempDir Path tempDir) throws Exception {
        BytesOut out = BytesOut.expandableArray();
        File testValue = new File(tempDir.toFile(), "test.txt").getAbsoluteFile();

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(File.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(testValue.getPath(), record.toString());
    }

    @Test
    void shouldRecordDirectory(@TempDir Path tempDir) throws Exception {
        BytesOut out = BytesOut.expandableArray();
        File testValue = tempDir.toFile();

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(File.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(testValue.getPath(), record.toString());
    }

    @Test
    void shouldRecordFileWithSpecialCharacters() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        File testValue = new File("test file with spaces.txt");

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(File.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(testValue.getPath(), record.toString());
    }

    @Test
    void shouldPreserveTypeInformation() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        File testValue = new File("test.txt");

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(File.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(File.class.getName(), record.getType().getName());
    }

    @Test
    void shouldRecordMultipleValuesConsistently() throws Exception {
        BytesOut out1 = BytesOut.expandableArray();
        BytesOut out2 = BytesOut.expandableArray();
        File testValue = new File("test.txt");
        
        recorder.write(testValue, out1, typeResolver);
        recorder.write(testValue, out2, typeResolver);

        ObjectRecord record1 = recorder.read(
            typeResolver.get(File.class),
            out1.flip(),
            typeResolver::getById
        );
        ObjectRecord record2 = recorder.read(
            typeResolver.get(File.class),
            out2.flip(),
            typeResolver::getById
        );

        assertEquals(record1.toString(), record2.toString());
        assertEquals(record1.getType(), record2.getType());
    }

    @Test
    void shouldHandleNullValue() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        File testValue = null;

        assertThrows(NullPointerException.class, () -> recorder.write(testValue, out, typeResolver));
    }

    @Test
    void shouldHandleNestedPaths() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        File testValue = new File(new File("parent"), "child/file.txt");

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(File.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(testValue.getPath(), record.toString());
    }
} 