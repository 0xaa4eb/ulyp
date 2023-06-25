package com.ulyp.agent;

import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

import com.ulyp.core.util.BitUtil;
import org.junit.Before;
import org.junit.Test;

import com.ulyp.agent.policy.EnabledByDefaultRecordingPolicy;
import com.ulyp.core.Method;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.RecordedMethodCall;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.util.ReflectionBasedMethodResolver;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.util.HeapRecordingDataWrtiter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RecorderTest {

    private static class X {
        public String foo(Integer s) {
            return s.toString();
        }
    }

    private final MethodRepository methodRepository = new MethodRepository();
    private final HeapRecordingDataWrtiter storage = new HeapRecordingDataWrtiter();
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final Recorder recorder = new Recorder(typeResolver, methodRepository, new EnabledByDefaultRecordingPolicy(), storage);
    private final ReflectionBasedMethodResolver methodResolver = new ReflectionBasedMethodResolver();
    private Method method;
    private int methodIdx;

    @Before
    public void setUp() throws NoSuchMethodException {
        method = methodResolver.resolve(X.class.getMethod("foo", Integer.class));
        methodIdx = methodRepository.putAndGetId(method);
    }

    @Test
    public void shouldRecordDataWhenRecordingIsFinished() {
        X recorded = new X();
        int callId = recorder.startOrContinueRecordingOnMethodEnter(methodIdx, recorded, new Object[5]);
        recorder.onMethodExit(methodIdx, "ABC", null, callId);

        assertNull(recorder.getRecordingState());
        assertEquals(2, storage.getCallRecords().size());
    }

    @Test
    public void testTemporaryRecordingDisableWithOngoingRecording() {

        X recorded = new X();
        int callId1 = recorder.startOrContinueRecordingOnMethodEnter(methodIdx, recorded, new Object[5]);

        recorder.disableRecording();

        int callId2 = recorder.onMethodEnter(methodIdx, recorded, new Object[]{10});
        recorder.onMethodExit(methodIdx, "CDE", null, callId2);

        recorder.enableRecording();

        recorder.onMethodExit(methodIdx, "ABC", null, callId1);

        assertEquals(2, storage.getCallRecords().size());

        // only the callId1 calls are recorded
        assertEquals(new HashSet<>(Collections.singletonList(BitUtil.longFromInts(0, callId1))), storage.getCallRecords()
            .stream()
            .map(RecordedMethodCall::getCallId)
            .collect(Collectors.toSet())
        );
    }

    @Test
    public void testTemporaryRecordingDisableWithNoOngoingRecording() {
        recorder.disableRecording();

        recorder.enableRecording();

        assertNull(recorder.getRecordingState());
    }
}