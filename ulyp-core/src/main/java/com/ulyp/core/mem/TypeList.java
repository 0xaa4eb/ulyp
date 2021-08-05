package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.Type;
import com.ulyp.transport.BinaryDataDecoder;
import com.ulyp.transport.BinaryTypeDecoder;
import com.ulyp.transport.BinaryTypeEncoder;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.jetbrains.annotations.NotNull;

public class TypeList implements Iterable<Type> {

    private final BinaryTypeEncoder binaryTypeEncoder = new BinaryTypeEncoder();
    private BinaryList bytes = new BinaryList();

    public void add(Type type) {
        bytes.add(
                encoder -> {
                    MutableDirectBuffer wrappedBuffer = encoder.buffer();
                    int headerLength = 4;
                    int limit = encoder.limit();
                    binaryTypeEncoder.wrap(wrappedBuffer, limit + headerLength);
                    type.serialize(binaryTypeEncoder);
                    int typeSerializedLength = binaryTypeEncoder.encodedLength();
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
    public AddressableItemIterator<Type> iterator() {
        AddressableItemIterator<BinaryDataDecoder> iterator = bytes.iterator();
        return new AddressableItemIterator<Type>() {
            @Override
            public long address() {
                return iterator.address();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Type next() {
                BinaryDataDecoder decoder = iterator.next();
                UnsafeBuffer buffer = new UnsafeBuffer();
                decoder.wrapValue(buffer);
                BinaryTypeDecoder typeDecoder = new BinaryTypeDecoder();
                typeDecoder.wrap(buffer, 0, BinaryTypeDecoder.BLOCK_LENGTH, 0);
                return Type.deserialize(typeDecoder);
            }
        };
    }
}
