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

public class StorageReadWriteTest {

    private StorageReader reader;
    private StorageWriter writer;

    @Before
    public void setUp() throws IOException {
        File file = Files.createTempFile(StorageReadWriteTest.class.getSimpleName(), "a").toFile();
        this.reader = new StorageReaderImpl(file);
        this.writer = new StorageWriterImpl(file);
    }

    @After
    public void tearDown() {
        reader.close();
        writer.close();
    }

    public static class T {
        public String foo(String in) {
            return in;
        }
    }

    @Test
    public void testReadWriteRecording() {
        int recordingId = 42;
        RecordingMetadata recordingMetadata = RecordingMetadata.builder()
                .id(recordingId)
                .createEpochMillis(2324L)
                .threadName("Thread-1")
                .threadId(4343L)
                .build();

        writer.write(
                RecordingMetadata.builder()
                        .id(recordingId)
                        .createEpochMillis(2324L)
                        .threadName("AAAA")
                        .threadId(4343L)
                        .build()
        );

        TypeResolver typeResolver = new ReflectionBasedTypeResolver();
        Type type = typeResolver.get(T.class);
        TypeList types = new TypeList();
        types.add(type);
        Method method = Method.builder()
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
        MethodList methods = new MethodList();
        methods.add(method);
        T callee = new T();

        RecordedMethodCallList methodCalls = new RecordedMethodCallList();

        methodCalls.addEnterMethodCall(
                recordingId,
                0,
                method,
                typeResolver,
                callee,
                new Object[] { "ABC" }
        );

        methodCalls.addExitMethodCall(
                recordingId,
                0,
                method,
                typeResolver,
                false,
                "DEF"
        );


        writer.write(recordingMetadata);
        writer.write(types);
        writer.write(methods);
        writer.write(methodCalls);


        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(
                        () -> {
                            Assert.assertEquals(1, reader.availableRecordings().size());

                            Recording recording = reader.availableRecordings().get(0);
                            CallRecord root = recording.getRoot();
                            Assert.assertNotNull(root);

                            Assert.assertEquals(1, root.getArgs().size());
                            StringObjectRecord argRecorded = (StringObjectRecord) root.getArgs().get(0);
                            Assert.assertEquals("ABC", argRecorded.value());

                            IdentityObjectRecord calleeRecorded = (IdentityObjectRecord) root.getCallee();
                            Assert.assertEquals(System.identityHashCode(callee), calleeRecorded.getHashCode());
                        }
                );
    }

    @Test
    public void testReadWriteRecordingWithNoData() {

        writer.write(
                RecordingMetadata.builder()
                        .id(42)
                        .createEpochMillis(2324L)
                        .threadName("AAAA")
                        .threadId(4343L)
                        .build()
        );

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(
                        () -> {
                            Assert.assertEquals(1, reader.availableRecordings().size());
                        }
                );
    }
}