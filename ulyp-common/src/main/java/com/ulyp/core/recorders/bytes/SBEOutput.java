package com.ulyp.core.recorders.bytes;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public abstract class SBEOutput {

    private final byte[] tmp = new byte[32 * 1024]; // TODO configurable + growable
    protected final UnsafeBuffer buffer = new UnsafeBuffer(tmp);
    protected final SBEBufferBinaryOutput output = new SBEBufferBinaryOutput(buffer);

    public BinaryOutput output() {
        output.reset();
        return output;
    }

    protected abstract void write(MutableDirectBuffer buffer, int length);

    // Allows writing to SBEOutput.tmp buffer, then writes directly into SBE encoder.
    // TODO Zero-copy support is not yet investigated
    private class SBEBufferBinaryOutput extends BufferBinaryOutput {

        public SBEBufferBinaryOutput(MutableDirectBuffer buffer) {
            super(buffer);
        }

        @Override
        public void close() {
            super.close();
            if (recursionDepth() == 0) {
                SBEOutput.this.write(buffer, pos);
            }
        }
    }
}
