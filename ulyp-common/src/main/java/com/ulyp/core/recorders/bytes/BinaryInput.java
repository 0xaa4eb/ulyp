package com.ulyp.core.recorders.bytes;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.recorders.ObjectRecord;

public interface BinaryInput {

    static BinaryInput from(byte[] value) {
        return new BinaryInputImpl(value);
    }

    boolean readBoolean();

    byte readByte();

    int readInt();

    long readLong();

    ObjectRecord readObject(ByIdTypeResolver typeResolver);

    String readString();
}
