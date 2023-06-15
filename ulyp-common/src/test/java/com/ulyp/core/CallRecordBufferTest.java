package com.ulyp.core;

import org.junit.Assert;
import org.junit.Test;

import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.util.ReflectionBasedTypeResolver;

public class CallRecordBufferTest {

    public static class T {
        public String foo(String in) {
            return in;
        }
    }

    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final Type type = typeResolver.get(T.class);
    private final Method method = Method.builder()
        .declaringType(type)
        .name("run")
        .id(1000L)
        .isConstructor(false)
        .isStatic(false)
        .returnsSomething(true)
        .parameterRecorders(
            new ObjectRecorder[]{ObjectRecorderRegistry.STRING_RECORDER.getInstance()}
        )
        .returnValueRecorder(ObjectRecorderRegistry.STRING_RECORDER.getInstance())
        .build();

    @Test
    public void testTotalCallRecordedEnterCalls() {
        CallRecordBuffer log = new CallRecordBuffer(typeResolver, 0L);

        Assert.assertEquals(0, log.getTotalRecordedEnterCalls());
        Assert.assertEquals(0, log.getRecordedCallsSize());

        long callId = log.recordMethodEnter(1, method, new T(), new Object[]{"ABC"});

        Assert.assertEquals(1, log.getTotalRecordedEnterCalls());
        Assert.assertEquals(1, log.getRecordedCallsSize());

        CallRecordBuffer newLog = log.cloneWithoutData();

        Assert.assertEquals(1, newLog.getTotalRecordedEnterCalls());
        Assert.assertEquals(0, newLog.getRecordedCallsSize());

        newLog.recordMethodExit(1, method, "ABC", null, callId);

        Assert.assertEquals(1, newLog.getTotalRecordedEnterCalls());
        Assert.assertEquals(1, newLog.getRecordedCallsSize());

        newLog.recordMethodEnter(1, method, new T(), new Object[]{"ABC"});

        Assert.assertEquals(2, newLog.getTotalRecordedEnterCalls());
        Assert.assertEquals(2, newLog.getRecordedCallsSize());
    }
}