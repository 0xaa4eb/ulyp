package com.ulyp.core.recorders.basic;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EnumRecorderTest {

    private final EnumRecorder recorder = new EnumRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    private enum TestEnum {
        VALUE1,
        VALUE2,
        COMPLEX_VALUE_NAME
    }

    private enum EmptyEnum {}

    @Test
    void shouldSupportEnumTypes() {
        assertTrue(recorder.supports(TestEnum.class));
        assertTrue(recorder.supports(TimeUnit.class));
        assertTrue(recorder.supports(EmptyEnum.class));
        
        assertFalse(recorder.supports(String.class));
        assertFalse(recorder.supports(Object.class));
        assertFalse(recorder.supports(Integer.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @Test
    void shouldRecordSimpleEnum() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        TestEnum testValue = TestEnum.VALUE1;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(TestEnum.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(EnumRecord.class, record);
        assertEquals(testValue.name(), record.toString());
    }

    @Test
    void shouldRecordComplexEnumName() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        TestEnum testValue = TestEnum.COMPLEX_VALUE_NAME;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(TestEnum.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(testValue.name(), record.toString());
    }

    @Test
    void shouldRecordStandardJavaEnum() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        TimeUnit testValue = TimeUnit.MILLISECONDS;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(TimeUnit.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(testValue.name(), record.toString());
    }

    @Test
    void shouldPreserveTypeInformation() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        TestEnum testValue = TestEnum.VALUE1;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(TestEnum.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(TestEnum.class.getName(), record.getType().getName());
    }

    @Test
    void shouldRecordMultipleValuesConsistently() throws Exception {
        BytesOut out1 = BytesOut.expandableArray();
        BytesOut out2 = BytesOut.expandableArray();
        TestEnum testValue = TestEnum.VALUE2;
        
        recorder.write(testValue, out1, typeResolver);
        recorder.write(testValue, out2, typeResolver);

        ObjectRecord record1 = recorder.read(
            typeResolver.get(TestEnum.class),
            out1.flip(),
            typeResolver::getById
        );
        ObjectRecord record2 = recorder.read(
            typeResolver.get(TestEnum.class),
            out2.flip(),
            typeResolver::getById
        );

        assertEquals(record1.toString(), record2.toString());
        assertEquals(record1.getType(), record2.getType());
    }

    @Test
    void shouldHandleNullValue() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        TestEnum testValue = null;

        assertThrows(NullPointerException.class, () -> recorder.write(testValue, out, typeResolver));
    }

    private enum EnumWithFields {
        INSTANCE("test") {
            @Override
            public String toString() {
                return "Custom toString";
            }
        };

        private final String value;

        EnumWithFields(String value) {
            this.value = value;
        }
    }

    @Test
    void shouldHandleEnumWithCustomToString() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        EnumWithFields testValue = EnumWithFields.INSTANCE;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(EnumWithFields.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(testValue.name(), record.toString());
    }
} 