package com.ulyp.core.printers.bytes;

import com.ulyp.transport.TCallExitRecordEncoder;
import org.agrona.concurrent.UnsafeBuffer;

public class BinaryOutputForExitRecordImpl extends AbstractBinaryOutput {

    private TCallExitRecordEncoder encoder;

    public void wrap(TCallExitRecordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void write(UnsafeBuffer unsafeBuffer, int length) {
        int headerLength = 4;
        final int limit = encoder.limit();
        encoder.limit(limit + headerLength + length);
        encoder.buffer().putInt(limit, length, java.nio.ByteOrder.LITTLE_ENDIAN);
        encoder.buffer().putBytes(limit + headerLength, unsafeBuffer, 0, length);
    }
}
