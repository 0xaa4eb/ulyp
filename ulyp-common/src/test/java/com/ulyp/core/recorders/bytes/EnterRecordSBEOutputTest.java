package com.ulyp.core.recorders.bytes;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import com.ulyp.transport.BinaryRecordedExitMethodCallDecoder;
import com.ulyp.transport.BinaryRecordedExitMethodCallEncoder;

public class EnterRecordSBEOutputTest {

    private final byte[] buf = new byte[16 * 1024];
    private final UnsafeBuffer unsafeBuffer = new UnsafeBuffer();
    private final BinaryRecordedExitMethodCallEncoder encoder = new BinaryRecordedExitMethodCallEncoder();
    private final BinaryRecordedExitMethodCallDecoder decoder = new BinaryRecordedExitMethodCallDecoder();

    @Test
    public void test() {
        unsafeBuffer.wrap(buf);
        encoder.wrap(unsafeBuffer, 0);
        decoder.wrap(unsafeBuffer, 0, BinaryRecordedExitMethodCallDecoder.BLOCK_LENGTH, 0);

        ExitRecordSBEOutput sbeOutput = new ExitRecordSBEOutput();
        try (BinaryOutput output = sbeOutput.wrap(encoder)) {
            Checkpoint checkpoint = output.checkpoint();
            output.write("ABC");
            checkpoint.rollback();
            output.write("CDE");
        }

        UnsafeBuffer buffer = new UnsafeBuffer(new byte[10]);
        decoder.wrapReturnValueBytes(buffer);

        System.out.println(buffer.capacity());
        byte[] bytes = "CDE".getBytes(StandardCharsets.UTF_8);
        System.out.println(Arrays.toString(bytes));
        System.out.println(Arrays.toString(buffer.byteArray()));
    }
}