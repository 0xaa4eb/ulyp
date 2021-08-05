package com.ulyp.core;

import com.ulyp.transport.BinaryMethodDecoder;
import com.ulyp.transport.BinaryMethodEncoder;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class MethodTest {

    private final UnsafeBuffer buffer = new UnsafeBuffer(new byte[1024 * 16]);

    @Test
    public void testSerialization() {
        verifySerilization(
                Method.builder()
                        .declaringType(Type.builder()
                                .id(534L)
                                .name("java.lang.RuntimeException")
                                .superTypeNames(new HashSet<>(Arrays.asList("java.lang.Throwable", "java.lang.Exception")))
                                .superTypeSimpleNames(new HashSet<>(Arrays.asList("Throwable", "Exception")))
                                .build())
                        .name("run")
                        .id(5552312L)
                        .isConstructor(false)
                        .isStatic(true)
                        .returnsSomething(true)
                        .build()
        );
    }

    public void verifySerilization(Method method) {
        BinaryMethodEncoder encoder = new BinaryMethodEncoder();
        encoder.wrap(buffer, 0);
        int sbeBlockLength = encoder.sbeBlockLength();

        method.serialize(encoder);

        BinaryMethodDecoder decoder = new BinaryMethodDecoder();
        decoder.wrap(buffer, 0, sbeBlockLength, 0);

        Method deserialized = Method.deserialize(decoder);

        Assert.assertEquals(method.getName(), deserialized.getName());
        Assert.assertEquals(method.getId(), deserialized.getId());
        Assert.assertEquals(method.isConstructor(), deserialized.isConstructor());
        Assert.assertEquals(method.isStatic(), deserialized.isStatic());
        Assert.assertEquals(method.returnsSomething(), deserialized.returnsSomething());

        Assert.assertEquals(method.getDeclaringType().getName(), deserialized.getDeclaringType().getName());
        Assert.assertEquals(method.getDeclaringType().getId(), deserialized.getDeclaringType().getId());
        Assert.assertEquals(method.getDeclaringType().getSuperTypeNames(), deserialized.getDeclaringType().getSuperTypeNames());
    }
}