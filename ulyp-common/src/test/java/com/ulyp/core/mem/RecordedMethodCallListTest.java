package com.ulyp.core.mem;

import com.ulyp.core.*;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class RecordedMethodCallListTest {

    public static class A {
        public String convert(int x) {
            return String.valueOf(x);
        }
    }

    private final RecordedMethodCallList list = new RecordedMethodCallList();
    private final ReflectionBasedTypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    public void testAddAndIterate() {

        Type type = typeResolver.get(A.class);

        Method method = Method.builder()
                .id(5L)
                .name("convert")
                .declaringType(type)
                .parameterRecorders(new ObjectRecorder[]{ObjectRecorderRegistry.ANY_NUMBER_RECORDER.getInstance()})
                .returnValueRecorder(ObjectRecorderRegistry.STRING_RECORDER.getInstance())
                .build();

        list.addEnterMethodCall(
                5,
                134L,
                method,
                typeResolver,
                new A(),
                new Object[]{5}
        );

        list.addExitMethodCall(
                5,
                134L,
                method,
                typeResolver,
                false,
                "ABC"
        );


        List<RecordedMethodCall> calls = list.stream().collect(Collectors.toList());

        RecordedEnterMethodCall recordedEnterMethodCall = (RecordedEnterMethodCall) calls.get(0);

        Assert.assertEquals(5, recordedEnterMethodCall.getRecordingId());
        Assert.assertEquals(134L, recordedEnterMethodCall.getCallId());

        RecordedExitMethodCall recordedExitMethodCall = (RecordedExitMethodCall) calls.get(1);

        Assert.assertEquals(5, recordedExitMethodCall.getRecordingId());
        Assert.assertEquals(134L, recordedExitMethodCall.getCallId());
    }
}