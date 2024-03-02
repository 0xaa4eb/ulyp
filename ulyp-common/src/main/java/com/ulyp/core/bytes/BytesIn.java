package com.ulyp.core.bytes;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.recorders.ObjectRecord;

public interface BytesIn {

    int available();

    boolean readBoolean();

    byte readByte();

    int readInt();

    int readInt(int offset);

    char readChar();

    long readLong();

    int getPosition();

    void moveTo(int position);

    BytesIn readBytes();

    BytesIn readBytes(int offset, int length);

    ObjectRecord readObject(ByIdTypeResolver typeResolver);

    String readString();

    int readIntAt(int offset);
}
