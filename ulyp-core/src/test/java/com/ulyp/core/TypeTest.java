package com.ulyp.core;

import com.ulyp.transport.BinaryTypeDecoder;
import com.ulyp.transport.BinaryTypeEncoder;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

public class TypeTest {

    private final UnsafeBuffer buffer = new UnsafeBuffer(new byte[1024 * 4]);

    @Test
    public void testSerialization() {

        verifySerilization(
                Type.builder()
                        .id(34L)
                        .name("java.lang.RuntimeException")
                        .superTypeNames(new HashSet<>())
                        .superTypeSimpleNames(new HashSet<>())
                        .build()
        );

        verifySerilization(
                Type.builder()
                        .id(534L)
                        .name("java.lang.RuntimeException")
                        .superTypeNames(new HashSet<>(Arrays.asList("java.lang.Throwable", "java.lang.Exception")))
                        .superTypeSimpleNames(new HashSet<>(Arrays.asList("Throwable", "Exception")))
                        .build()
        );
    }

    public void verifySerilization(Type type) {
        BinaryTypeEncoder encoder = new BinaryTypeEncoder();
        encoder.wrap(buffer, 0);
        int sbeBlockLength = encoder.sbeBlockLength();

        type.serialize(encoder);

        BinaryTypeDecoder decoder = new BinaryTypeDecoder();
        decoder.wrap(buffer, 0, sbeBlockLength, 0);

        Type deserialized = Type.deserialize(decoder);

        Assert.assertEquals(type.getName(), deserialized.getName());
        Assert.assertEquals(type.getId(), deserialized.getId());
        Assert.assertEquals(type.getSuperTypeNames(), deserialized.getSuperTypeNames());
        Assert.assertEquals(type.getSuperTypeSimpleNames(), deserialized.getSuperTypeSimpleNames());
    }
}