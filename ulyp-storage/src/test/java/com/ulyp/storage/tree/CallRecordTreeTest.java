package com.ulyp.storage.tree;

import com.ulyp.core.Method;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.mem.SerializedMethodList;
import com.ulyp.core.mem.SerializedRecordedMethodCallList;
import com.ulyp.core.mem.SerializedTypeList;
import com.ulyp.core.recorders.basic.StringObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.reader.FileRecordingDataReaderBuilder;
import com.ulyp.storage.reader.RecordingDataReader;
import com.ulyp.storage.util.TestMemPageAllocator;
import com.ulyp.storage.writer.FileRecordingDataWriter;
import com.ulyp.storage.writer.RecordingDataWriter;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class CallRecordTreeTest {

    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final Type type = typeResolver.get(T.class);
    private final Method method = Method.builder()
        .type(type)
        .name("run")
        .id(1000)
        .constructor(false)
        .isStatic(false)
        .returnsSomething(true)
        .build();
    private final SerializedTypeList types = new SerializedTypeList();
    private final SerializedMethodList methods = new SerializedMethodList();
    private final T obj = new T();
    private RecordingDataReader reader;
    private RecordingDataWriter writer;

    public static class T {
        public String foo(String in) {
            return in;
        }
    }

    @BeforeEach
    public void setUp() throws IOException {
        File file = Files.createTempFile(CallRecordTreeTest.class.getSimpleName(), "a").toFile();
        reader = new FileRecordingDataReaderBuilder(file).build();
        writer = new FileRecordingDataWriter(file);
        types.add(type);
        methods.add(method);
    }

    @AfterEach
    public void tearDown() {
        reader.close();
        writer.close();
    }

    @Test
    void testEmptyTree() throws Exception {
        try (CallRecordTree tree = new CallRecordTreeBuilder(reader)
            .setIndexSupplier(InMemoryIndex::new)
            .setReadInfinitely(false)
            .build()) {

            assertEquals(0, tree.getRecordings().size());
        }
    }

    @Test
    void testReadWriteRecordingWithoutReturnValue() throws ExecutionException, InterruptedException {
        SerializedRecordedMethodCallList calls = new SerializedRecordedMethodCallList(1, new TestMemPageAllocator());
        calls.addEnterMethodCall(method.getId(), typeResolver, obj, new Object[]{"ABC"});
        calls.addExitMethodCall(1, typeResolver, "CDE");

        writer.write(RecordingMetadata.builder().id(1).build());
        writer.write(types);
        writer.write(methods);
        writer.write(calls);
        writer.close();

        CallRecordTree tree = new CallRecordTreeBuilder(reader)
            .setIndexSupplier(InMemoryIndex::new)
            .setReadInfinitely(false)
            .build();
        tree.getCompleteFuture().get();

        assertEquals(1, tree.getRecordings().size());

        Recording recording = tree.getRecordings().iterator().next();
        CallRecord root = recording.getRoot();
        assertTrue(root.isFullyRecorded());

        StringObjectRecord returnValue = (StringObjectRecord) root.getReturnValue();
        MatcherAssert.assertThat(returnValue.value(), Matchers.is("CDE"));
    }

    @Test
    void testNotFinishedRecording() throws ExecutionException, InterruptedException {
        SerializedRecordedMethodCallList calls = new SerializedRecordedMethodCallList(1, new TestMemPageAllocator());
        calls.addEnterMethodCall(method.getId(), typeResolver, obj, new Object[]{"ABC"});

        writer.write(RecordingMetadata.builder().id(1).build());
        writer.write(types);
        writer.write(methods);
        writer.write(calls);
        writer.close();

        CallRecordTree tree = new CallRecordTree(reader, RecordingListener.empty(), InMemoryIndex::new, true);
        tree.getCompleteFuture().get();

        assertEquals(1, tree.getRecordings().size());

        Recording recording = tree.getRecordings().iterator().next();
        CallRecord root = recording.getRoot();
        assertFalse(root.isFullyRecorded());
    }
}
