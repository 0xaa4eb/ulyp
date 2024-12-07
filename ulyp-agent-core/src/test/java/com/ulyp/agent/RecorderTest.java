package com.ulyp.agent;

import com.ulyp.agent.options.AgentOptions;
import com.ulyp.agent.policy.AlwaysEnabledRecordingPolicy;
import com.ulyp.agent.queue.RecordingEventQueue;
import com.ulyp.core.Method;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.metrics.NullMetrics;
import com.ulyp.core.util.ReflectionBasedMethodResolver;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.writer.HeapRecordingDataWrtiter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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
    private final RecordingEventQueue callRecordQueue = new RecordingEventQueue(
            typeResolver,
            new AgentDataWriter(storage, methodRepository),
            new NullMetrics()
    );
    private final Recorder recorder = new Recorder(
            new AgentOptions(),
            typeResolver,
            methodRepository,
            new AlwaysEnabledRecordingPolicy(),
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
        long callToken = recorder.startRecordingOnMethodEnter(methodIdx, recorded, new Object[] {5});
        recorder.onMethodExit(methodIdx, "ABC", null, callToken);
        callRecordQueue.sync(Duration.ofSeconds(5));

        long callToken2 = recorder.startRecordingOnMethodEnter(methodIdx, recorded, new Object[] {5});
        recorder.onMethodExit(methodIdx, "ABC", null, callToken2);
        callRecordQueue.sync(Duration.ofSeconds(5));

        assertNull(recorder.getRecordingCtx());
        assertEquals(4, storage.getCallRecords().size());
    }

    @Test
    void testTemporaryRecordingDisableWithOngoingRecording() throws InterruptedException, TimeoutException {
        X recorded = new X();
        long callId1 = recorder.startRecordingOnMethodEnter(methodIdx, recorded, new Object[] {5});

        recorder.disableRecording();

        long callId2 = recorder.onMethodEnter(methodIdx, recorded, new Object[]{10});
        Assertions.assertEquals(-1, callId2);
        recorder.onMethodExit(methodIdx, "CDE", null, callId2);

        recorder.enableRecording();

        recorder.onMethodExit(methodIdx, "ABC", null, callId1);
        callRecordQueue.sync(Duration.ofSeconds(5));

        assertEquals(2, storage.getCallRecords().size());

        // only the callId1 calls are recorded
        assertEquals(new HashSet<>(Collections.singletonList((int) callId1)), storage.getCallRecords()
            .stream()
            .filter(call -> call instanceof RecordedExitMethodCall)
            .map(call -> (int) ((RecordedExitMethodCall) call).getCallId())
            .collect(Collectors.toSet()));
    }

    @Test
    void testTemporaryRecordingDisableWithNoOngoingRecording() {
        recorder.disableRecording();

        recorder.enableRecording();

        assertNull(recorder.getRecordingCtx());
    }
}
