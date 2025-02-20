package com.ulyp.core.recorders.basic;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class PathRecorderTest {

    private final PathRecorder recorder = new PathRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldRecordSimplePath() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Path testPath = Paths.get("simple", "test", "path");

        recorder.write(testPath, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Path.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(FileRecord.class, objectRecord);
        FileRecord fileRecord = (FileRecord) objectRecord;
        assertEquals(testPath.toString(), fileRecord.getPath());
    }

    @Test
    void shouldRecordAbsolutePath(@TempDir Path tempDir) throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Path absolutePath = tempDir.resolve("absolute/test/path").toAbsolutePath();

        recorder.write(absolutePath, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Path.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(FileRecord.class, objectRecord);
        FileRecord fileRecord = (FileRecord) objectRecord;
        assertEquals(absolutePath.toString(), fileRecord.getPath());
    }

    @Test
    void shouldRecordPathWithSpecialCharacters() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Path testPath = Paths.get("path", "with spaces!", "#$%^&");

        recorder.write(testPath, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Path.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(FileRecord.class, objectRecord);
        FileRecord fileRecord = (FileRecord) objectRecord;
        assertEquals(testPath.toString(), fileRecord.getPath());
    }

    @Test
    void shouldRecordPathWithUnicodeCharacters() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Path testPath = Paths.get("路径", "путь", "パス");

        recorder.write(testPath, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Path.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(FileRecord.class, objectRecord);
        FileRecord fileRecord = (FileRecord) objectRecord;
        assertEquals(testPath.toString(), fileRecord.getPath());
    }

    @Test
    void shouldRecordEmptyPath() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Path testPath = Paths.get("");

        recorder.write(testPath, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Path.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(FileRecord.class, objectRecord);
        FileRecord fileRecord = (FileRecord) objectRecord;
        assertEquals(testPath.toString(), fileRecord.getPath());
    }

    @Test
    void shouldOnlySupportPathClass() {
        assertTrue(recorder.supports(Path.class));
        assertFalse(recorder.supports(Object.class));
        assertFalse(recorder.supports(String.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @Test
    void shouldRecordRootPath() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Path testPath = Paths.get("/");

        recorder.write(testPath, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Path.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(FileRecord.class, objectRecord);
        FileRecord fileRecord = (FileRecord) objectRecord;
        assertEquals(testPath.toString(), fileRecord.getPath());
    }

    @Test
    void shouldRecordNormalizedPath() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Path testPath = Paths.get("path/../normalized/./path").normalize();

        recorder.write(testPath, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Path.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(FileRecord.class, objectRecord);
        FileRecord fileRecord = (FileRecord) objectRecord;
        assertEquals(testPath.toString(), fileRecord.getPath());
    }

    @Test
    void shouldRecordPathWithFileExtension() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Path testPath = Paths.get("test/file.txt");

        recorder.write(testPath, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Path.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(FileRecord.class, objectRecord);
        FileRecord fileRecord = (FileRecord) objectRecord;
        assertEquals(testPath.toString(), fileRecord.getPath());
    }
} 