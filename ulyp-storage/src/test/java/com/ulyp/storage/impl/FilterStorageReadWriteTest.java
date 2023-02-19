package com.ulyp.storage.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ulyp.core.Method;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.ReaderSettings;
import com.ulyp.storage.StorageReader;
import com.ulyp.storage.StorageWriter;

public class FilterStorageReadWriteTest {

    private final int recordingId = 42;
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final Type type = typeResolver.get(T.class);
    private final Method method = Method.builder()
            .declaringType(type)
            .implementingType(type)
            .name("run")
            .id(1000L)
            .isConstructor(false)
            .isStatic(false)
            .returnsSomething(true)
            .parameterRecorders(
                    new ObjectRecorder[]{ObjectRecorderRegistry.STRING_RECORDER.getInstance()}
            )
            .returnValueRecorder(ObjectRecorderRegistry.STRING_RECORDER.getInstance())
            .build();
    private final TypeList types = new TypeList();
    private final MethodList methods = new MethodList();
    private StorageReader reader;
    private StorageWriter writer;
    private RecordingMetadata recordingMetadata;
    private File file;

    @Before
    public void setUp() throws IOException {
        file = Files.createTempFile(FilterStorageReadWriteTest.class.getSimpleName(), "a").toFile();
        this.writer = new FileStorageWriter(file);

        recordingMetadata = RecordingMetadata.builder()
                .id(recordingId)
                .recordingStartedEpochMillis(System.currentTimeMillis())
                .threadName("Thread-1")
                .threadId(4343L)
                .build();

        types.add(type);
        methods.add(method);

    }

    @After
    public void tearDown() {
        reader.close();
        writer.close();
    }

    @Test
    public void test() {
        this.reader = new AsyncFileStorageReader(
            ReaderSettings.builder()
                .file(file)
                .autoStartReading(true)
                .filter(
                    recording -> recording.callCount() > 5
                )
                .build());

        T callee = new T();

        RecordedMethodCallList methodCalls = new RecordedMethodCallList();

        methodCalls.addEnterMethodCall(
                recordingId,
                0,
                method,
                typeResolver,
                callee,
                new Object[]{"ABC"}
        );
        methodCalls.addEnterMethodCall(
            recordingId,
            1,
            method,
            typeResolver,
            callee,
            new Object[]{"ABC"}
        );
        methodCalls.addEnterMethodCall(
            recordingId,
            2,
            method,
            typeResolver,
            callee,
            new Object[]{"ABC"}
        );
        methodCalls.addEnterMethodCall(
            recordingId,
            3,
            method,
            typeResolver,
            callee,
            new Object[]{"ABC"}
        );

        writer.write(recordingMetadata);
        writer.write(types);
        writer.write(methods);
        writer.write(methodCalls);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals(0, reader.getRecordings().size());
    }

    public static class T {
        public String foo(String in) {
            return in;
        }
    }
}