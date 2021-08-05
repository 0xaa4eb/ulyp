package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.Method;
import com.ulyp.transport.BinaryDataDecoder;
import com.ulyp.transport.BinaryMethodDecoder;
import com.ulyp.transport.BinaryMethodEncoder;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.jetbrains.annotations.NotNull;

public class MethodList implements Iterable<Method> {

    private final BinaryMethodEncoder methodEncoder = new BinaryMethodEncoder();
    private BinaryList bytes = new BinaryList();

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
