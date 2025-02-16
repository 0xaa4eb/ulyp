package com.ulyp.core.recorders.numeric;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class DoubleRecorderTest {

    private final DoubleRecorder recorder = new DoubleRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldOnlySupportDoubleClass() {
        assertTrue(recorder.supports(Double.class));
        assertFalse(recorder.supports(Integer.class));
        assertFalse(recorder.supports(Float.class));
        assertFalse(recorder.supports(String.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.0, -1.0, Double.MAX_VALUE, Double.MIN_VALUE, Math.PI})
    void shouldRecordDoubleValues(double value) throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Double testValue = value;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Double.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(NumberRecord.class, record);
        assertEquals(String.valueOf(value), record.toString());
    }

    @Test
    void shouldHandleSpecialValues() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        
        recorder.write(Double.POSITIVE_INFINITY, out, typeResolver);
        ObjectRecord record = recorder.read(
            typeResolver.get(Double.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals("Infinity", record.toString());

        out = BytesOut.expandableArray();
        recorder.write(Double.NEGATIVE_INFINITY, out, typeResolver);
        record = recorder.read(
            typeResolver.get(Double.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals("-Infinity", record.toString());

        out = BytesOut.expandableArray();
        recorder.write(Double.NaN, out, typeResolver);
        record = recorder.read(
            typeResolver.get(Double.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals("NaN", record.toString());
    }

    @Test
    void shouldMaintainPrecision() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Double testValue = 1.23456789;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Double.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(String.valueOf(testValue), record.toString());
    }
} 