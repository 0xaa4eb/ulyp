package com.ulyp.core.printers.bytes;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BinaryInputOutputTest {

    private final UnsafeBuffer buffer = new UnsafeBuffer(new byte[16 * 1024]);

    private final BinaryOutput binaryOutput = new AbstractBinaryOutput() {
        @Override
        public void write(UnsafeBuffer unsafeBuffer, int length) {
            buffer.putBytes(0, unsafeBuffer, 0, length);
        }
    };

    private final BinaryInput binaryInput = new BinaryInputImpl(buffer);

    @Test
    public void testSimpleReadWriteString() throws Exception {
        try (BinaryOutputAppender appender = binaryOutput.appender()) {
            appender.append("abc");
        }

        assertEquals("abc", binaryInput.readString());
    }

    @Test
    public void testRollingBack() throws Exception {
        try (BinaryOutputAppender appender = binaryOutput.appender()) {
            Checkpoint checkpoint = appender.checkpoint();
            appender.append("abc");
            checkpoint.rollback();

            try (BinaryOutputAppender nested = binaryOutput.appender()) {
                nested.append("xyz");
            }
        }

        assertEquals("xyz", binaryInput.readString());
    }

    @Test
    public void testSimpleReadWriteUtf8String() throws Exception {
        try (BinaryOutputAppender appender = binaryOutput.appender()) {
            appender.append("АБЦ");
        }

        assertEquals("АБЦ", binaryInput.readString());
    }

    @Test
    public void testSimpleReadWriteUtf8StringChineese() throws Exception {
        try (BinaryOutputAppender appender = binaryOutput.appender()) {
            appender.append("早上好");
        }

        assertEquals("早上好", binaryInput.readString());
    }

    @Test
    public void testComplexReadWrite() throws Exception {
        try (BinaryOutputAppender appender = binaryOutput.appender()) {
            appender.append(2L);
            appender.append(null);
            appender.append(6L);
        }

        assertEquals(2, binaryInput.readLong());
        assertNull(binaryInput.readString());
        assertEquals(6, binaryInput.readLong());
    }

    @Test
    public void testNestedAppender() throws Exception {
        try (BinaryOutputAppender appender = binaryOutput.appender()) {
            appender.append(2L);
            try (BinaryOutputAppender appender2 = appender.appender()) {
                appender2.append("你吃了吗?");
            }
            appender.append(6L);
            appender.append(null);
        }

        assertEquals(2, binaryInput.readLong());
        assertEquals("你吃了吗?", binaryInput.readString());
        assertEquals(6, binaryInput.readLong());
        assertNull(binaryInput.readString());
    }
}
