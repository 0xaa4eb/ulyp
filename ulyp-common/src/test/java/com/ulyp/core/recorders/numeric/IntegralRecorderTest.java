package com.ulyp.core.recorders.numeric;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class IntegralRecorderTest {

    private final IntegralRecorder recorder = new IntegralRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldSupportIntegralTypes() {
        assertTrue(recorder.supports(Long.class));
        assertTrue(recorder.supports(Integer.class));
        assertTrue(recorder.supports(Short.class));
        assertTrue(recorder.supports(Byte.class));
        
        assertFalse(recorder.supports(Float.class));
        assertFalse(recorder.supports(Double.class));
        assertFalse(recorder.supports(String.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE})
    void shouldRecordLongValues(long value) throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Long testValue = value;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Long.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(IntegralRecord.class, record);
        assertEquals(value, ((IntegralRecord) record).getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE})
    void shouldRecordIntegerValues(int value) throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Integer testValue = value;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Integer.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(IntegralRecord.class, record);
        assertEquals(value, ((IntegralRecord) record).getValue());
    }

    @ParameterizedTest
    @ValueSource(shorts = {0, 1, -1, Short.MAX_VALUE, Short.MIN_VALUE})
    void shouldRecordShortValues(short value) throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Short testValue = value;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Short.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(IntegralRecord.class, record);
        assertEquals(value, ((IntegralRecord) record).getValue());
    }

    @ParameterizedTest
    @ValueSource(bytes = {0, 1, -1, Byte.MAX_VALUE, Byte.MIN_VALUE})
    void shouldRecordByteValues(byte value) throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Byte testValue = value;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Byte.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(IntegralRecord.class, record);
        assertEquals(value, ((IntegralRecord) record).getValue());
    }

    @Test
    void shouldPreserveTypeInformation() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Byte byteValue = 42;
        recorder.write(byteValue, out, typeResolver);
        ObjectRecord record = recorder.read(
            typeResolver.get(Byte.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals(Byte.class.getName(), record.getType().getName());

        out = BytesOut.expandableArray();
        Long longValue = 42L;
        recorder.write(longValue, out, typeResolver);
        record = recorder.read(
            typeResolver.get(Long.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals(Long.class.getName(), record.getType().getName());
    }
} 