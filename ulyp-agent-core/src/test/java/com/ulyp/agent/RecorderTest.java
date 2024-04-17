package com.ulyp.agent;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.ulyp.agent.queue.RecordingQueue;
import com.ulyp.core.metrics.NullMetrics;
import com.ulyp.storage.writer.HeapRecordingDataWrtiter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.AfterEach;

import com.ulyp.agent.policy.EnabledRecordingPolicy;
import com.ulyp.core.Method;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.RecordedMethodCall;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.util.ReflectionBasedMethodResolver;
import com.ulyp.core.util.ReflectionBasedTypeResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecorderTest {

    private static class X {
        public String foo(Integer s) {
            return s.toString();
        }
    }

    private final MethodRepository methodRepository = new MethodRepository();
    private final HeapRecordingDataWrtiter storage = new HeapRecordingDataWrtiter();
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final RecordingQueue callRecordQueue = new RecordingQueue(typeResolver, new AgentDataWriter(storage, methodRepository), new NullMetrics());
    private final Recorder recorder = new Recorder(
            methodRepository,
            new EnabledRecordingPolicy(),
            callRecordQueue,
            new NullMetrics());
    private final ReflectionBasedMethodResolver methodResolver = new ReflectionBasedMethodResolver();
    private Method method;
    private int methodIdx;

    @BeforeEach
    public void setUp() throws NoSuchMethodException {
        method = methodResolver.resolve(X.class.getMethod("foo", Integer.class));
        methodIdx = methodRepository.putAndGetId(method);

        callRecordQueue.start();
    }

    @AfterEach
    public void tearDown() {
        callRecordQueue.close();
    }

    @Test
    void shouldRecordDataWhenRecordingIsFinished() throws InterruptedException, TimeoutException {
        X recorded = new X();
        int callId = recorder.startOrContinueRecordingOnMethodEnter(methodIdx, recorded, new Object[] {5});
        recorder.onMethodExit(methodIdx, "ABC", null, callId);
        callRecordQueue.sync(Duration.ofSeconds(5));

        assertNull(recorder.getRecordingState());
        assertEquals(2, storage.getCallRecords().size());
    }

    @Test
    void testTemporaryRecordingDisableWithOngoingRecording() throws InterruptedException, TimeoutException {
        Recorder.recordingIdGenerator.set(0);

        X recorded = new X();
        int callId1 = recorder.startOrContinueRecordingOnMethodEnter(methodIdx, recorded, new Object[] {5});

        recorder.disableRecording();

        int callId2 = recorder.onMethodEnter(methodIdx, recorded, new Object[]{10});
        recorder.onMethodExit(methodIdx, "CDE", null, callId2);

        recorder.enableRecording();

        recorder.onMethodExit(methodIdx, "ABC", null, callId1);
        callRecordQueue.sync(Duration.ofSeconds(5));

        assertEquals(2, storage.getCallRecords().size());

        // only the callId1 calls are recorded
        assertEquals(new HashSet<>(Collections.singletonList((long) callId1)), storage.getCallRecords()
            .stream()
            .map(RecordedMethodCall::getCallId)
            .collect(Collectors.toSet()));
    }

    @Test
    void testTemporaryRecordingDisableWithNoOngoingRecording() {
        recorder.disableRecording();

        recorder.enableRecording();

        assertNull(recorder.getRecordingState());
    }
}
