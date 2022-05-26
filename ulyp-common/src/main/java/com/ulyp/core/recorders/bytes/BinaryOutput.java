package com.ulyp.core.recorders.bytes;

public interface BinaryOutput {

    int recursionDepth();

    BinaryOutputAppender appender();

    Checkpoint checkpoint();

    void writeBool(boolean val);

    void writeChar(char val);

    void writeInt(int val);

    void writeLong(long val);

    void writeString(final String value)w;
}
