package com.ulyp.core.recorders.bytes;

import com.ulyp.transport.BinaryRecordedExitMethodCallEncoder;

import org.agrona.MutableDirectBuffer;

public class ExitRecordSBEOutput extends SBEOutput {

    private BinaryRecordedExitMethodCallEncoder encoder;

    public BinaryOutput wrap(BinaryRecordedExitMethodCallEncoder encoder) {
        this.encoder = encoder;
        return output();
    }

    @Override
    public void write(MutableDirectBuffer buffer, int length) {
        int headerLength = 4;
        final int limit = encoder.limit();
        encoder.limit(limit + headerLength + length);
        encoder.buffer().putInt(limit, length, java.nio.ByteOrder.LITTLE_ENDIAN);
        encoder.buffer().putBytes(limit + headerLength, buffer, 0, length);
    }
}
