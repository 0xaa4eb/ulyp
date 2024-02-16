package com.ulyp.storage.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ulyp.core.Method;
import com.ulyp.core.RecordedMethodCall;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.util.StubRecordingDataReaderJob;
import com.ulyp.storage.writer.RecordingDataWriter;
import com.ulyp.storage.writer.FileRecordingDataWriter;

public class FilterStorageReadWriteTest {

    private final T callee = new T();
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final Type type = typeResolver.get(T.class);
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
    private File file;

    public static class T {
        public String foo(String in) {
            return in;
        }
    }

    @Before
    public void setUp() throws IOException {
        file = Files.createTempFile(FilterStorageReadWriteTest.class.getSimpleName(), "a").toFile();
        writer = new FileRecordingDataWriter(file);
        types.add(type);
        methods.add(method);
    }

    @After
    public void tearDown() throws InterruptedException {
        reader.close();
        writer.close();
    }

    @Test
    public void testReaderJobSubmit() throws ExecutionException, InterruptedException, TimeoutException {
        this.reader = new FileRecordingDataReaderBuilder(file)
            .build();

        RecordedMethodCallList methodCalls1 = new RecordedMethodCallList(1);

        methodCalls1.addEnterMethodCall(0, method, typeResolver, callee, new Object[]{"ABC"});
        methodCalls1.addEnterMethodCall(1, method, typeResolver, callee, new Object[]{"ABC"});
        methodCalls1.addEnterMethodCall(2, method, typeResolver, callee, new Object[]{"ABC"});
        methodCalls1.addEnterMethodCall(3, method, typeResolver, callee, new Object[]{"ABC"});

        RecordedMethodCallList methodCalls2 = new RecordedMethodCallList(1);
        methodCalls2.addExitMethodCall(3, typeResolver, "BVBC");
        methodCalls2.addExitMethodCall(2, typeResolver, "XCZXC");

        writer.write(types);
        writer.write(methods);

        writer.write(RecordingMetadata.builder().id(1).build());
        writer.write(methodCalls1);
        writer.write(methodCalls2);
        writer.close();

        StubRecordingDataReaderJob job = new StubRecordingDataReaderJob();
        CompletableFuture<Void> voidCompletableFuture = reader.submitReaderJob(job);
        voidCompletableFuture.get(1, TimeUnit.MINUTES);

        Assert.assertEquals(1, job.getRecordingMetadatas().size());

        Type typeDeserialized = job.getTypes().get(type.getId());
        Assert.assertEquals(typeDeserialized, type);

        Assert.assertEquals(1, job.getMethods().size());
        Method methodDeserialized = job.getMethods().get(method.getId());
        Assert.assertEquals(methodDeserialized, method);

        Map<Integer, List<RecordedMethodCall>> recordedCalls = job.getRecordedCalls();
        List<RecordedMethodCall> calls = recordedCalls.get(1);

        Assert.assertEquals(6, calls.size());
    }
}
