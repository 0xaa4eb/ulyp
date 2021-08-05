package com.ulyp.core.printers.bytes;

import com.ulyp.transport.TCallEnterRecordEncoder;
import org.agrona.concurrent.UnsafeBuffer;

public class BinaryOutputForEnterRecordImpl extends AbstractBinaryOutput {

    private TCallEnterRecordEncoder encoder;

    public void wrap(TCallEnterRecordEncoder encoder) {
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
