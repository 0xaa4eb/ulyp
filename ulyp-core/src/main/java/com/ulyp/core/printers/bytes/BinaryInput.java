package com.ulyp.core.printers.bytes;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.printers.ObjectRepresentation;

public interface BinaryInput {

    static BinaryInput from(byte[] value) {
        return new BinaryInputImpl(value);
    }

    boolean readBoolean();

    byte readByte();

    int readInt();

    long readLong();

    ObjectRepresentation readObject(ByIdTypeResolver typeResolver);

    String readString();
}
