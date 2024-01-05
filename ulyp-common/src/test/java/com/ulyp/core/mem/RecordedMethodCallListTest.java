package com.ulyp.core.mem;

import com.ulyp.core.*;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class RecordedMethodCallListTest {

    private final RecordedMethodCallList list = new RecordedMethodCallList(432);
    private final ReflectionBasedTypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    public void testAddAndIterate() {
        list.addEnterMethodCall(134, 5, typeResolver, new A(), new Object[]{5});
        list.addExitMethodCall(134, typeResolver, "ABC");

        List<RecordedMethodCall> calls = list.stream().collect(Collectors.toList());

        RecordedEnterMethodCall recordedEnterMethodCall = (RecordedEnterMethodCall) calls.get(0);

        Assert.assertEquals(134, recordedEnterMethodCall.getCallId());

        RecordedExitMethodCall recordedExitMethodCall = (RecordedExitMethodCall) calls.get(1);

        Assert.assertEquals(134, recordedExitMethodCall.getCallId());
    }

    @Test
    public void testSerialization() {
        BinaryList rawBytes = list.getRawBytes();

        RecordedMethodCallList recordedMethodCalls = new RecordedMethodCallList(rawBytes);

        Assert.assertEquals(432, recordedMethodCalls.getRecordingId());
    }

    public static class A {
        public String convert(int x) {
            return String.valueOf(x);
        }
    }
}