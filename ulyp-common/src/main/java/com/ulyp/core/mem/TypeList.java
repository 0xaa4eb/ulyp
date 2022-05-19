package com.ulyp.core.mem;

import com.google.common.base.Preconditions;
import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.Type;
import com.ulyp.transport.BinaryDataDecoder;
import com.ulyp.transport.BinaryTypeDecoder;
import com.ulyp.transport.BinaryTypeEncoder;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.jetbrains.annotations.NotNull;

public class TypeList implements Iterable<Type> {

    public static final int WIRE_ID = 1;

    private final BinaryTypeEncoder binaryTypeEncoder = new BinaryTypeEncoder();
    private final BinaryList bytes;

    public TypeList() {
        bytes = new BinaryList(WIRE_ID);
    }

    public TypeList(BinaryList bytes) {
        Preconditions.checkArgument(bytes.id() == WIRE_ID, "Invalid binary list passed");
        this.bytes = bytes;
    }

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

    public int byteLength() {
        return bytes.byteLength();
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
