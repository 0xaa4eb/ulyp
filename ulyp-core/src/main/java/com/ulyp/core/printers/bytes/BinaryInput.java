package com.ulyp.core.printers.bytes;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.printers.ObjectRecord;

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
