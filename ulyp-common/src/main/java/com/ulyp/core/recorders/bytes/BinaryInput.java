package com.ulyp.core.recorders.bytes;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.recorders.ObjectRecord;

public interface BinaryInput {

    boolean readBoolean();

    byte readByte();

    int readInt();

    char readChar();

    long readLong();

    ObjectRecord readObject(ByIdTypeResolver typeResolver);

    String readString();
}
