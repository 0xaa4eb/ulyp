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

    public void writeBool(boolean val) {
        try (BinaryOutputAppender appender = appender()) {
            appender.append(val);
        }
    }

    public void writeChar(char val) {
        try (BinaryOutputAppender appender = appender()) {
            appender.append(val);
        }
    }

    public void writeInt(int val) {
        try (BinaryOutputAppender appender = appender()) {
            appender.append(val);
        }
    }

    public void writeLong(long val) {
        try (BinaryOutputAppender appender = appender()) {
            appender.append(val);
        }
    }

    public void writeString(final String value) {
        try (BinaryOutputAppender appender = appender()) {
            appender.append(value);
        }
    }

    public abstract void write(final UnsafeBuffer unsafeBuffer, int length);
}
