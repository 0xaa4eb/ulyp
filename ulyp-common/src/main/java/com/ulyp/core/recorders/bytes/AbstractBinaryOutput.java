package com.ulyp.core.recorders.bytes;

import org.agrona.concurrent.UnsafeBuffer;

public abstract class AbstractBinaryOutput implements BinaryOutput {

    private final BinaryOutputAppender appender = new BinaryOutputAppender(this);

    @Override
    public int recursionDepth() {
        return 0;
    }

    public BinaryOutputAppender appender() {
        appender.reset();
        return appender;
    }

    @Override
    public Checkpoint checkpoint() {
        return appender.checkpoint();
    }

    public void writeBool(boolean val) throws Exception {
        try (BinaryOutputAppender appender = appender()) {
            appender.append(val);
        }
    }

    public void writeChar(char val) throws Exception {
        try (BinaryOutputAppender appender = appender()) {
            appender.append(val);
        }
    }

    public void writeInt(int val) throws Exception {
        try (BinaryOutputAppender appender = appender()) {
            appender.append(val);
        }
    }

    public void writeLong(long val) throws Exception {
        try (BinaryOutputAppender appender = appender()) {
            appender.append(val);
        }
    }

    public void writeString(final String value) throws Exception {
        try (BinaryOutputAppender appender = appender()) {
            appender.append(value);
        }
    }

    public abstract void write(final UnsafeBuffer unsafeBuffer, int length);
}
