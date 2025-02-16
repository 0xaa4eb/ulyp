package com.ulyp.core.recorders.numeric;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class FloatRecorderTest {

    private final FloatRecorder recorder = new FloatRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldOnlySupportFloatClass() {
        assertTrue(recorder.supports(Float.class));
        assertFalse(recorder.supports(Integer.class));
        assertFalse(recorder.supports(Double.class));
        assertFalse(recorder.supports(String.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @ParameterizedTest
    @ValueSource(floats = {0.0f, 1.0f, -1.0f, Float.MAX_VALUE, Float.MIN_VALUE})
    void shouldRecordFloatValues(float value) throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Float testValue = value;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Float.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(NumberRecord.class, record);
        assertEquals(String.valueOf(value), record.toString());
    }

    @Test
    void shouldHandleSpecialValues() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        
        recorder.write(Float.POSITIVE_INFINITY, out, typeResolver);
        ObjectRecord record = recorder.read(
            typeResolver.get(Float.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals("Infinity", record.toString());

        out = BytesOut.expandableArray();
        recorder.write(Float.NEGATIVE_INFINITY, out, typeResolver);
        record = recorder.read(
            typeResolver.get(Float.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals("-Infinity", record.toString());

        out = BytesOut.expandableArray();
        recorder.write(Float.NaN, out, typeResolver);
        record = recorder.read(
            typeResolver.get(Float.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals("NaN", record.toString());
    }

    @Test
    void shouldMaintainPrecision() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Float testValue = 1.234567f;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Float.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(String.valueOf(testValue), record.toString());
    }

    @Test
    void shouldHandlePiValue() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Float testValue = (float) Math.PI;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Float.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(String.valueOf(testValue), record.toString());
    }
} 