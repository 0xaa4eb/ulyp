package com.ulyp.core.recorders.bytes;

public interface BinaryOutput {

    int recursionDepth();

    BinaryOutputAppender appender();

    Checkpoint checkpoint();

    void writeBool(boolean val) throws Exception;

    void writeChar(char val) throws Exception;

    void writeInt(int val) throws Exception;

    void writeLong(long val) throws Exception;

    void writeString(final String value) throws Exception;
}
