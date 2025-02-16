package com.ulyp.core.recorders.basic;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NullObjectRecorderTest {

    private final NullObjectRecorder recorder = new NullObjectRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldNotSupportAnyType() {
        assertFalse(recorder.supports(Object.class));
        assertFalse(recorder.supports(String.class));
        assertFalse(recorder.supports(Integer.class));
        assertFalse(recorder.supports(void.class));
        assertFalse(recorder.supports(null));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @Test
    void shouldAlwaysReturnNullObjectRecord() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        recorder.write(null, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Object.class),
            out.flip(),
            typeResolver::getById
        );

        assertSame(NullObjectRecord.getInstance(), record);
        assertEquals("null", record.toString());
    }

    @Test
    void shouldReturnSameInstanceForMultipleReads() throws Exception {
        BytesOut out1 = BytesOut.expandableArray();
        BytesOut out2 = BytesOut.expandableArray();
        
        recorder.write(null, out1, typeResolver);
        recorder.write(null, out2, typeResolver);

        ObjectRecord record1 = recorder.read(
            typeResolver.get(Object.class),
            out1.flip(),
            typeResolver::getById
        );
        ObjectRecord record2 = recorder.read(
            typeResolver.get(Object.class),
            out2.flip(),
            typeResolver::getById
        );

        assertSame(record1, record2);
        assertSame(NullObjectRecord.getInstance(), record1);
    }

    @Test
    void shouldHandleNonNullValues() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Object testValue = new Object();

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Object.class),
            out.flip(),
            typeResolver::getById
        );

        assertSame(NullObjectRecord.getInstance(), record);
    }

    @Test
    void shouldHandleDifferentTypes() throws Exception {
        Class<?>[] types = {
            String.class,
            Integer.class,
            Boolean.class,
            Object.class,
            void.class
        };

        for (Class<?> type : types) {
            BytesOut out = BytesOut.expandableArray();
            recorder.write(null, out, typeResolver);

            ObjectRecord record = recorder.read(
                typeResolver.get(type),
                out.flip(),
                typeResolver::getById
            );

            assertSame(NullObjectRecord.getInstance(), record);
            assertEquals("null", record.toString());
        }
    }

    @Test
    void shouldWriteConsistentBytes() throws Exception {
        BytesOut out1 = BytesOut.expandableArray();
        BytesOut out2 = BytesOut.expandableArray();

        recorder.write(null, out1, typeResolver);
        recorder.write(null, out2, typeResolver);

        assertArrayEquals(out1.flip().toByteArray(), out2.flip().toByteArray());
    }

    @Test
    void shouldHandleMultipleWrites() throws Exception {
        BytesOut out = BytesOut.expandableArray();

        for (int i = 0; i < 100; i++) {
            recorder.write(null, out, typeResolver);
        }

        out.flip();
        for (int i = 0; i < 100; i++) {
            ObjectRecord record = recorder.read(
                typeResolver.get(Object.class),
                out.flip(),
                typeResolver::getById
            );
            assertSame(NullObjectRecord.getInstance(), record);
        }
    }
} 