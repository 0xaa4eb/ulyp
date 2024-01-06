package com.ulyp.core;

import org.junit.Assert;
import org.junit.Test;

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
        .id(1000)
        .isConstructor(false)
        .isStatic(false)
        .returnsSomething(true)
        .build();

    @Test
    public void testTotalCallRecordedEnterCalls() {
        CallRecordBuffer log = new CallRecordBuffer(45645);

        Assert.assertEquals(0, log.getTotalRecordedEnterCalls());
        Assert.assertEquals(0, log.getRecordedCallsSize());

        int callId = log.recordMethodEnter(typeResolver, method.getId(), new T(), new Object[]{"ABC"});

        Assert.assertEquals(1, log.getTotalRecordedEnterCalls());
        Assert.assertEquals(1, log.getRecordedCallsSize());

        CallRecordBuffer newLog = log.cloneWithoutData();

        Assert.assertEquals(1, newLog.getTotalRecordedEnterCalls());
        Assert.assertEquals(0, newLog.getRecordedCallsSize());

        newLog.recordMethodExit(typeResolver, "ABC", null, callId);

        Assert.assertEquals(1, newLog.getTotalRecordedEnterCalls());
        Assert.assertEquals(1, newLog.getRecordedCallsSize());

        newLog.recordMethodEnter(typeResolver, method.getId(), new T(), new Object[]{"ABC"});

        Assert.assertEquals(2, newLog.getTotalRecordedEnterCalls());
        Assert.assertEquals(2, newLog.getRecordedCallsSize());
    }
}