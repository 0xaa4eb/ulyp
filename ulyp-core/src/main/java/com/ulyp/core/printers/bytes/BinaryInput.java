package com.ulyp.core.printers.bytes;

import com.ulyp.core.DecodingContext;
import com.ulyp.core.printers.ObjectRepresentation;

public interface BinaryInput {

    boolean readBoolean();

    byte readByte();

    int readInt();

    long readLong();

    ObjectRepresentation readObject(DecodingContext decodingContext);

    String readString();
}
