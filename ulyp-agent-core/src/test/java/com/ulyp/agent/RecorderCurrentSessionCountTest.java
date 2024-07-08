package com.ulyp.agent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ulyp.core.metrics.NullMetrics;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.StaticLoggerBinder;
import org.slf4j.spi.LocationAwareLogger;

import com.ulyp.agent.log.SimpleLoggerFactory;
import com.ulyp.agent.policy.EnabledRecordingPolicy;
import com.ulyp.agent.queue.RecordingEventQueue;
import com.ulyp.core.Method;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.util.ReflectionBasedMethodResolver;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.writer.BlackholeRecordingDataWriter;
import com.ulyp.storage.writer.StatsRecordingDataWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecorderCurrentSessionCountTest {

    private static final int THREADS = 10;
    private static final int RECORDINGS_PER_THREAD = 20000;

    private static class X {
        public String foo(Integer s) {
            return s.toString();
        }
    }

    private final MethodRepository methodRepository = new MethodRepository();
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final StatsRecordingDataWriter recordingDataWriter = new StatsRecordingDataWriter(new NullMetrics(), new BlackholeRecordingDataWriter());
    private final RecordingEventQueue recordingEventQueue = new RecordingEventQueue(typeResolver, new AgentDataWriter(recordingDataWriter, methodRepository), new NullMetrics());
    private final Recorder recorder = new Recorder(methodRepository, new EnabledRecordingPolicy(), recordingEventQueue, new NullMetrics());
    private final ReflectionBasedMethodResolver methodResolver = new ReflectionBasedMethodResolver();
    private Method method;
    private int methodIdx;
    private ExecutorService executor;

    @BeforeEach
    public void setUp() throws NoSuchMethodException {
        method = methodResolver.resolve(X.class.getMethod("foo", Integer.class));
        methodIdx = methodRepository.putAndGetId(method);
        executor = Executors.newFixedThreadPool(THREADS);
        recordingEventQueue.start();
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        recordingEventQueue.close();

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
                int callId = recorder.startRecordingOnMethodEnter(methodIdx, callee, new Object[5]);

                Assertions.assertTrue(Recorder.currentRecordingSessionCount.get() > 0, "Since at least one recording session is active, " +
                    "Recorder.currentRecordingSessionCount must be positive");

                recorder.onMethodExit(methodIdx, "ABC", null, callId);
            }
        }
    }

    @Test
    @Disabled
    void testCurrentRecordingSessionCountValueUnderConcurrentRecordings() throws ExecutionException, InterruptedException, TimeoutException {
        ((SimpleLoggerFactory) StaticLoggerBinder.getSingleton().getLoggerFactory()).setCurrentLogLevel(LocationAwareLogger.ERROR_INT);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            futures.add(executor.submit(new RecordingWorker(RECORDINGS_PER_THREAD)));
        }
        for (Future<?> fut : futures) {
            fut.get(1, TimeUnit.MINUTES);
        }

        recordingEventQueue.sync(Duration.ofMinutes(3));
        assertEquals(0, Recorder.currentRecordingSessionCount.get());
    }
}