package com.ulyp.core.recorders.numeric;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class NumbersRecorderTest {

    private final NumbersRecorder recorder = new NumbersRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldSupportAllNumberTypes() {
        assertTrue(recorder.supports(Number.class));
        assertTrue(recorder.supports(Integer.class));
        assertTrue(recorder.supports(Long.class));
        assertTrue(recorder.supports(Double.class));
        assertTrue(recorder.supports(Float.class));
        assertTrue(recorder.supports(Short.class));
        assertTrue(recorder.supports(Byte.class));
        assertTrue(recorder.supports(BigInteger.class));
        assertTrue(recorder.supports(BigDecimal.class));
        
        assertFalse(recorder.supports(String.class));
        assertFalse(recorder.supports(Object.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.0, -1.0, Math.PI, Double.MAX_VALUE, Double.MIN_VALUE})
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
    void shouldHandleBigNumbers() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        BigInteger bigInt = new BigInteger("123456789012345678901234567890");
        recorder.write(bigInt, out, typeResolver);
        ObjectRecord record = recorder.read(
            typeResolver.get(BigInteger.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals(bigInt.toString(), record.toString());

        out = BytesOut.expandableArray();
        BigDecimal bigDec = new BigDecimal("123456.789012345678901234567890");
        recorder.write(bigDec, out, typeResolver);
        record = recorder.read(
            typeResolver.get(BigDecimal.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals(bigDec.toString(), record.toString());
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
    void shouldPreserveTypeInformation() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Integer intValue = 42;
        recorder.write(intValue, out, typeResolver);
        ObjectRecord record = recorder.read(
            typeResolver.get(Integer.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals(Integer.class.getName(), record.getType().getName());

        out = BytesOut.expandableArray();
        BigDecimal bigDecValue = new BigDecimal("42.42");
        recorder.write(bigDecValue, out, typeResolver);
        record = recorder.read(
            typeResolver.get(BigDecimal.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals(BigDecimal.class.getName(), record.getType().getName());
    }

    @Test
    void shouldHandleCustomNumberClass() throws Exception {
        class CustomNumber extends Number {
            @Override
            public int intValue() { return 42; }
            @Override
            public long longValue() { return 42L; }
            @Override
            public float floatValue() { return 42.0f; }
            @Override
            public double doubleValue() { return 42.0; }
            @Override
            public String toString() { return "CustomNumber:42"; }
        }

        BytesOut out = BytesOut.expandableArray();
        CustomNumber customNumber = new CustomNumber();
        
        recorder.write(customNumber, out, typeResolver);
        
        ObjectRecord record = recorder.read(
            typeResolver.get(CustomNumber.class),
            out.flip(),
            typeResolver::getById
        );
        
        assertEquals("CustomNumber:42", record.toString());
    }
} 