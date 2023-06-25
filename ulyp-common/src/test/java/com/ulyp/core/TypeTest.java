package com.ulyp.core;

import com.ulyp.transport.BinaryTypeDecoder;
import com.ulyp.transport.BinaryTypeEncoder;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Test;

public class TypeTest {

    private final UnsafeBuffer buffer = new UnsafeBuffer(new byte[1024 * 4]);

    @Test
    public void testSerialization() {

        verifySerilization(
                Type.builder()
                        .id(34)
                        .name("java.lang.RuntimeException")
                        .build()
        );

        verifySerilization(
                Type.builder()
                        .id(534)
                        .name("java.lang.RuntimeException")
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
    }
}