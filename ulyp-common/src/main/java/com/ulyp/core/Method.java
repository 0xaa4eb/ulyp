package com.ulyp.core;

import com.ulyp.transport.*;
import lombok.Builder;
import lombok.ToString;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

@Builder
@ToString
public class Method {

    private final int id;
    private final String name;
    private final Type declaringType;
    private final boolean isStatic;
    private final boolean isConstructor;
    private final boolean returnsSomething;
    private volatile boolean shouldStartRecording;

    private static void writeType(BinaryMethodEncoder targetEncoder, Type type) {
        BinaryTypeEncoder binaryTypeEncoder = new BinaryTypeEncoder();
        MutableDirectBuffer wrappedBuffer = targetEncoder.buffer();
        int headerLength = 4;
        int limit = targetEncoder.limit();
        binaryTypeEncoder.wrap(wrappedBuffer, limit + headerLength);
        type.serialize(binaryTypeEncoder);
        int typeSerializedLength = binaryTypeEncoder.encodedLength();
        targetEncoder.limit(limit + headerLength + typeSerializedLength);
        wrappedBuffer.putInt(limit, typeSerializedLength, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public static Method deserialize(BinaryMethodDecoder decoder) {

        String name = decoder.name();

        UnsafeBuffer buffer = new UnsafeBuffer();
        BinaryTypeDecoder typeDecoder = new BinaryTypeDecoder();

        decoder.wrapDeclaringTypeValue(buffer);
        typeDecoder.wrap(buffer, 0, BinaryTypeEncoder.BLOCK_LENGTH, 0);
        Type declaringType = Type.deserialize(typeDecoder);

        return Method.builder()
                .id(decoder.id())
                .name(name)
                .declaringType(declaringType)
                .isStatic(decoder.staticFlag() == BooleanType.T)
                .isConstructor(decoder.constructor() == BooleanType.T)
                .returnsSomething(decoder.returnsSomething() == BooleanType.T)
                .build();
    }

    public boolean shouldStartRecording() {
        return shouldStartRecording;
    }

    public void setShouldStartRecording(boolean shouldStartRecording) {
        this.shouldStartRecording = shouldStartRecording;
    }

    public int getId() {
        return id;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public Type getDeclaringType() {
        return declaringType;
    }

    public String getName() {
        return name;
    }

    public boolean returnsSomething() {
        return returnsSomething;
    }

    public String toShortString() {
        return declaringType.getName() + "." + name;
    }

    public void serialize(BinaryMethodEncoder encoder) {
        encoder.id(this.id);
        encoder.returnsSomething(this.returnsSomething ? BooleanType.T : BooleanType.F);
        encoder.staticFlag(this.isStatic ? BooleanType.T : BooleanType.F);
        encoder.constructor(this.isConstructor ? BooleanType.T : BooleanType.F);

        encoder.name(this.name);
        writeType(encoder, declaringType);
    }
}
