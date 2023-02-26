package com.ulyp.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.impl.StaticLoggerBinder;
import org.slf4j.spi.LocationAwareLogger;

import com.ulyp.agent.log.SimpleLoggerFactory;
import com.ulyp.agent.policy.EnabledByDefaultRecordingPolicy;
import com.ulyp.core.Method;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.util.ReflectionBasedMethodResolver;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.impl.DevNullStorageWriter;
import com.ulyp.storage.impl.StatsRecordingStorageWriter;

public class RecorderCurrentSessionCountTest {

    private static final int THREADS = 10;
    private static final int RECORDINGS_PER_THREAD = 20000;

    private static class X {
        public String foo(Integer s) {
            return s.toString();
        }
    }

    private final StatsRecordingStorageWriter storageWriter = new StatsRecordingStorageWriter(new DevNullStorageWriter());
    private final Recorder recorder = new Recorder(new CallIdGenerator(), new EnabledByDefaultRecordingPolicy(), storageWriter);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final ReflectionBasedMethodResolver methodResolver = new ReflectionBasedMethodResolver();
    private Method method;
    private ExecutorService executor;

    @Before
    public void setUp() throws NoSuchMethodException {
        method = methodResolver.resolve(X.class.getMethod("foo", Integer.class));
        executor = Executors.newFixedThreadPool(THREADS);
    }

    @After
    public void tearDown() throws InterruptedException {
        ((SimpleLoggerFactory) StaticLoggerBinder.getSingleton().getLoggerFactory()).setCurrentLogLevel(LocationAwareLogger.INFO_INT);

        executor.shutdownNow();
        executor.awaitTermination(1, TimeUnit.HOURS);
    }

    private class RecordingWorker implements Runnable {

        private final int recordingsCount;

        private RecordingWorker(int recordingsCount) {
            this.recordingsCount = recordingsCount;
        }

        @Override
        public void run() {
            X callee = new X();

            for (int i = 0; i < recordingsCount && !Thread.currentThread().isInterrupted(); i++) {
                long callId = recorder.startOrContinueRecordingOnMethodEnter(typeResolver, method, callee, new Object[5]);

                Assert.assertTrue("Since at least one recording session is active, " +
                    "Recorder.currentRecordingSessionCount must be positive",
                    Recorder.currentRecordingSessionCount.get() > 0);

                recorder.onMethodExit(typeResolver, method, "ABC", null, callId);
            }
        }
    }

    @Test
    public void testCurrentRecordingSessionCountValueUnderConcurrentRecordings() throws ExecutionException, InterruptedException, TimeoutException {
        ((SimpleLoggerFactory) StaticLoggerBinder.getSingleton().getLoggerFactory()).setCurrentLogLevel(LocationAwareLogger.ERROR_INT);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            futures.add(executor.submit(new RecordingWorker(RECORDINGS_PER_THREAD)));
        }
        for (Future<?> fut : futures) {
            fut.get(1, TimeUnit.MINUTES);
        }

        Assert.assertEquals(THREADS * RECORDINGS_PER_THREAD, storageWriter.getCallStats().getTotalCount());
        Assert.assertEquals(0, Recorder.currentRecordingSessionCount.get());
    }
}