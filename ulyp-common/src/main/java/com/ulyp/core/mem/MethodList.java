package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.Method;
import com.ulyp.core.util.Preconditions;
import com.ulyp.transport.BinaryDataDecoder;
import com.ulyp.transport.BinaryMethodDecoder;
import com.ulyp.transport.BinaryMethodEncoder;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * A list of serialized {@link Method} instances
 */
public class MethodList implements Iterable<Method> {

    public static final int WIRE_ID = 3;

    private final BinaryMethodEncoder methodEncoder = new BinaryMethodEncoder();

    private final BinaryList bytes;

    public MethodList() {
        bytes = new BinaryList(WIRE_ID);
    }

    public MethodList(BinaryList bytes) {
        Preconditions.checkArgument(bytes.id() == WIRE_ID, "Invalid binary list passed");
        this.bytes = bytes;
    }

    public void add(Method method) {
        bytes.add(
                encoder -> {
                    MutableDirectBuffer wrappedBuffer = encoder.buffer();
                    int headerLength = 4;
                    int limit = encoder.limit();
                    methodEncoder.wrap(wrappedBuffer, limit + headerLength);
                    method.serialize(methodEncoder);
                    int typeSerializedLength = methodEncoder.encodedLength();
                    encoder.limit(limit + headerLength + typeSerializedLength);
                    wrappedBuffer.putInt(limit, typeSerializedLength, java.nio.ByteOrder.LITTLE_ENDIAN);
                }
        );
    }

    public int size() {
        return bytes.size();
    }

    public int byteLength() {
        return bytes.byteLength();
    }

    public BinaryList getRawBytes() {
        return bytes;
    }

    @NotNull
    @Override
    public AddressableItemIterator<Method> iterator() {
        AddressableItemIterator<BinaryDataDecoder> iterator = bytes.iterator();
        return new AddressableItemIterator<Method>() {
            @Override
            public long address() {
                return iterator.address();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Method next() {
                BinaryDataDecoder decoder = iterator.next();
                UnsafeBuffer buffer = new UnsafeBuffer();
                decoder.wrapValue(buffer);
                BinaryMethodDecoder typeDecoder = new BinaryMethodDecoder();
                typeDecoder.wrap(buffer, 0, BinaryMethodDecoder.BLOCK_LENGTH, 0);
                return Method.deserialize(typeDecoder);
            }
        };
    }
}
