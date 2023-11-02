package com.ulyp.storage.tree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ulyp.core.Method;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.NotRecordedObjectRecord;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.ReaderSettings;
import com.ulyp.storage.Recording;
import com.ulyp.storage.RecordingDataReader;
import com.ulyp.storage.RecordingDataWriter;
import com.ulyp.storage.RecordingListener;
import com.ulyp.storage.impl.AsyncFileRecordingDataReader;
import com.ulyp.storage.impl.FileRecordingDataWriter;
import com.ulyp.storage.impl.StorageReadWriteTest;
import com.ulyp.storage.impl.util.InMemoryIndex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class CallRecordTreeTest {

    private final int recordingId = 42;
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final Type type = typeResolver.get(StorageReadWriteTest.T.class);
    private final Method method = Method.builder()
        .declaringType(type)
        .name("run")
        .id(1000)
        .isConstructor(false)
        .isStatic(false)
        .returnsSomething(true)
        .build();
    private final TypeList types = new TypeList();
    private final MethodList methods = new MethodList();
    private RecordingDataReader reader;
    private RecordingDataWriter writer;
    private RecordingMetadata recordingMetadata;

    @Before
    public void setUp() throws IOException {
        File file = Files.createTempFile(StorageReadWriteTest.class.getSimpleName(), "a").toFile();
        this.reader = new AsyncFileRecordingDataReader(ReaderSettings.builder().file(file).autoStartReading(true).build());
        this.writer = new FileRecordingDataWriter(file);

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
    public void testReadWriteRecordingWithoutReturnValue() {
        StorageReadWriteTest.T callee = new StorageReadWriteTest.T();

        RecordedMethodCallList methodCalls = new RecordedMethodCallList();

        methodCalls.addEnterMethodCall(
            recordingId,
            0,
            method,
            typeResolver,
            callee,
            new Object[]{"ABC"}
        );
        methodCalls.addExitMethodCall(
            recordingId,
            0,
            method,
            typeResolver,
            callee,
            new Object[]{"ABC"}
        );

        writer.write(recordingMetadata);
        writer.write(types);
        writer.write(methods);
        writer.write(methodCalls);

        CallRecordTree callRecordTree = new CallRecordTree(reader, RecordingListener.empty(), InMemoryIndex::new);

    }
}
