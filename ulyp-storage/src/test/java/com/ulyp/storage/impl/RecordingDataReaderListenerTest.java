package com.ulyp.storage.impl;

import com.ulyp.core.*;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.CallRecord;
import com.ulyp.storage.ReaderSettings;
import com.ulyp.storage.Recording;
import com.ulyp.storage.RecordingDataWriter;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RecordingDataReaderListenerTest {

    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final Type type = typeResolver.get(T.class);
    private final Method method = Method.builder()
            .declaringType(type)
            .name("run")
            .id(1000)
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
    private AsyncFileRecordingDataReader reader;
    private RecordingDataWriter writer;
    private RecordingMetadata recordingMetadata1;
    private RecordingMetadata recordingMetadata2;

    @Before
    public void setUp() throws IOException {
        File file = Files.createTempFile(RecordingDataReaderListenerTest.class.getSimpleName(), "a").toFile();
        this.reader = new AsyncFileRecordingDataReader(ReaderSettings.builder().file(file).autoStartReading(false).build());
        this.writer = new FileRecordingDataWriter(file);

        recordingMetadata1 = RecordingMetadata.builder()
                .id(1)
                .threadName("Thread-1")
                .threadId(4343L)
                .build();

        recordingMetadata2 = RecordingMetadata.builder()
                .id(2)
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
    public void testAsyncReadOfProcessMetadata() {
        reader.start();

        writer.write(ProcessMetadata.builder()
                .mainClassName("a.b.C")
                .pid(555L)
                .classPathFiles(Arrays.asList("A", "B", "C"))
                .build()
        );

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(
                        () -> {
                            ProcessMetadata processMetadata = reader.getProcessMetadataFuture().getNow(null);

                            Assert.assertNotNull(processMetadata);

                            Assert.assertEquals("a.b.C", processMetadata.getMainClassName());
                            Assert.assertEquals(555L, processMetadata.getPid());
                            Assert.assertEquals(Arrays.asList("A", "B", "C"), processMetadata.getClassPathFiles());
                        }
                );
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
                new Object[]{"ABC"}
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
                new Object[]{"ABC"}
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

    public static class T {
        public String foo(String in) {
            return in;
        }
    }
}