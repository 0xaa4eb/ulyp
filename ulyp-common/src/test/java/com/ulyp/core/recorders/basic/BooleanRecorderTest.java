package com.ulyp.core.recorders.basic;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BooleanRecorderTest {

    private final BooleanRecorder recorder = new BooleanRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldSupportBooleanTypes() {
        assertTrue(recorder.supports(Boolean.class));
        assertTrue(recorder.supports(boolean.class));
        
        assertFalse(recorder.supports(Integer.class));
        assertFalse(recorder.supports(String.class));
        assertFalse(recorder.supports(Object.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldRecordBooleanValues(boolean value) throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Boolean testValue = value;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Boolean.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(BooleanRecord.class, record);
        assertEquals(value, ((BooleanRecord) record).getValue());
        assertEquals(String.valueOf(value), record.toString());
    }

    @Test
    void shouldPreserveTypeInformation() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Boolean testValue = true;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Boolean.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(Boolean.class.getName(), record.getType().getName());
    }

    @Test
    void shouldRecordMultipleValuesConsistently() throws Exception {
        BytesOut out1 = BytesOut.expandableArray();
        BytesOut out2 = BytesOut.expandableArray();
        
        recorder.write(true, out1, typeResolver);
        recorder.write(true, out2, typeResolver);

        ObjectRecord record1 = recorder.read(
            typeResolver.get(Boolean.class),
            out1.flip(),
            typeResolver::getById
        );
        ObjectRecord record2 = recorder.read(
            typeResolver.get(Boolean.class),
            out2.flip(),
            typeResolver::getById
        );

        assertEquals(record1.toString(), record2.toString());
        assertEquals(record1.getType(), record2.getType());
    }

    @Test
    void shouldHandleNullValue() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Boolean testValue = null;

        assertThrows(NullPointerException.class, () -> recorder.write(testValue, out, typeResolver));
    }
} 