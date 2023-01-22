package com.ulyp.core;

import org.junit.Assert;
import org.junit.Test;

import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.util.ReflectionBasedTypeResolver;

public class CallRecordLogTest {

    public static class T {
        public String foo(String in) {
            return in;
        }
    }

    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final Type type = typeResolver.get(T.class);
    private final Method method = Method.builder()
        .declaringType(type)
        .implementingType(type)
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
        CallRecordLog log = new CallRecordLog(typeResolver, 0L);

        Assert.assertEquals(0, log.getTotalRecordedEnterCalls());
        Assert.assertEquals(0, log.getRecordedCallsSize());

        long callId = log.onMethodEnter(method, new T(), new Object[]{"ABC"});

        Assert.assertEquals(1, log.getTotalRecordedEnterCalls());
        Assert.assertEquals(1, log.getRecordedCallsSize());

        CallRecordLog newLog = log.cloneWithoutData();

        Assert.assertEquals(1, newLog.getTotalRecordedEnterCalls());
        Assert.assertEquals(0, newLog.getRecordedCallsSize());

        newLog.onMethodExit(method, "ABC", null, callId);

        Assert.assertEquals(1, newLog.getTotalRecordedEnterCalls());
        Assert.assertEquals(1, newLog.getRecordedCallsSize());

        newLog.onMethodEnter(method, new T(), new Object[]{"ABC"});

        Assert.assertEquals(2, newLog.getTotalRecordedEnterCalls());
        Assert.assertEquals(2, newLog.getRecordedCallsSize());
    }
}