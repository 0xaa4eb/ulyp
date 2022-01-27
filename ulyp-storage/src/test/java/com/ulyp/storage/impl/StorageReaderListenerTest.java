package com.ulyp.storage.impl;

import com.ulyp.core.Method;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.recorders.*;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.CallRecord;
import com.ulyp.storage.Recording;
import com.ulyp.storage.StorageReader;
import com.ulyp.storage.StorageWriter;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.*;

public class StorageReaderListenerTest {

    private BackgroundThreadFileStorageReader reader;
    private StorageWriter writer;

    private RecordingMetadata recordingMetadata1;
    private RecordingMetadata recordingMetadata2;
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final Type type = typeResolver.get(T.class);
    private final Method method = Method.builder()
            .declaringType(type)
            .name("run")
            .id(1000L)
            .isConstructor(false)
            .isStatic(false)
            .returnsSomething(true)
            .parameterRecorders(
                    new ObjectRecorder[] { ObjectRecorderType.STRING_RECORDER.getInstance() }
            )
            .returnValueRecorder(ObjectRecorderType.STRING_RECORDER.getInstance())
            .build();
    private final TypeList types = new TypeList();
    private final MethodList methods = new MethodList();

    public static class T {
        public String foo(String in) {
            return in;
        }
    }

    @Before
    public void setUp() throws IOException {
        File file = Files.createTempFile(StorageReaderListenerTest.class.getSimpleName(), "a").toFile();
        this.reader = new BackgroundThreadFileStorageReader(file, false);
        this.writer = new FileStorageWriter(file);

        recordingMetadata1 = RecordingMetadata.builder()
                .id(1)
                .createEpochMillis(2324L)
                .threadName("Thread-1")
                .threadId(4343L)
                .build();

        recordingMetadata2 = RecordingMetadata.builder()
                .id(2)
                .createEpochMillis(2324L)
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
    public void testStorageReaderListener() {
        List<Recording> recordings = new CopyOnWriteArrayList<>();
        reader.subscribe(recordings::add);
        reader.start();

        T callee = new T();

        RecordedMethodCallList methodCalls = new RecordedMethodCallList();

        methodCalls.addEnterMethodCall(
                1,
                0,
                method,
                typeResolver,
                callee,
                new Object[] { "ABC" }
        );

        writer.write(recordingMetadata1);
        writer.write(types);
        writer.write(methods);
        writer.write(methodCalls);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(
                        () -> {
                            Assert.assertEquals(1, recordings.size());

                            CallRecord root = recordings.get(0).getRoot();
                            Assert.assertNotNull(root);
                        }
                );

        methodCalls = new RecordedMethodCallList();

        methodCalls.addEnterMethodCall(
                2,
                10,
                method,
                typeResolver,
                callee,
                new Object[] { "ABC" }
        );

        writer.write(recordingMetadata2);
        writer.write(methodCalls);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(
                        () -> {
                            Assert.assertEquals(2, recordings.size());

                            Assert.assertNotEquals(recordings.get(0).getId(), recordings.get(1).getId());
                        }
                );
    }
}