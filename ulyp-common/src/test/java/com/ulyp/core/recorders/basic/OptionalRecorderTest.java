package com.ulyp.core.recorders.basic;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OptionalRecorderTest {

    private final OptionalRecorder recorder = new OptionalRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldRecordEmptyOptional() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Optional<?> testOptional = Optional.empty();

        recorder.write(testOptional, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Optional.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(OptionalRecord.class, objectRecord);
        OptionalRecord optionalRecord = (OptionalRecord) objectRecord;
        assertTrue(optionalRecord.isEmpty());
        assertNull(optionalRecord.getValue());
    }

    @Test
    void shouldRecordOptionalWithStringValue() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Optional<String> testOptional = Optional.of("Test value");

        recorder.write(testOptional, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Optional.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(OptionalRecord.class, objectRecord);
        OptionalRecord optionalRecord = (OptionalRecord) objectRecord;
        assertFalse(optionalRecord.isEmpty());
        assertInstanceOf(StringObjectRecord.class, optionalRecord.getValue());
        assertEquals("Test value", ((StringObjectRecord) optionalRecord.getValue()).value());
    }

    @Test
    void shouldOnlySupportOptionalClass() {
        assertTrue(recorder.supports(Optional.class));
        assertFalse(recorder.supports(Object.class));
        assertFalse(recorder.supports(String.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        // Optional class is immutable, so it is safe to record asynchronously
        assertTrue(recorder.supportsAsyncRecording());
    }

    @Test
    void shouldRecordOptionalWithSpecialCharacters() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Optional<String> testOptional = Optional.of("Special chars: \n\t\r\"\\!@#$%^&*()");

        recorder.write(testOptional, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Optional.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(OptionalRecord.class, objectRecord);
        OptionalRecord optionalRecord = (OptionalRecord) objectRecord;
        assertFalse(optionalRecord.isEmpty());
        assertInstanceOf(StringObjectRecord.class, optionalRecord.getValue());
        assertEquals("Special chars: \n\t\r\"\\!@#$%^&*()", 
            ((StringObjectRecord) optionalRecord.getValue()).value());
    }

    @Test
    void shouldRecordOptionalWithUnicodeCharacters() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Optional<String> testOptional = Optional.of("Unicode: 你好世界 🌍 привет");

        recorder.write(testOptional, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Optional.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(OptionalRecord.class, objectRecord);
        OptionalRecord optionalRecord = (OptionalRecord) objectRecord;
        assertFalse(optionalRecord.isEmpty());
        assertInstanceOf(StringObjectRecord.class, optionalRecord.getValue());
        assertEquals("Unicode: 你好世界 🌍 привет", 
            ((StringObjectRecord) optionalRecord.getValue()).value());
    }

    @Test
    void shouldRecordOptionalWithCustomObject() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        CustomObject customObject = new CustomObject("test");
        Optional<CustomObject> testOptional = Optional.of(customObject);

        recorder.write(testOptional, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Optional.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(OptionalRecord.class, objectRecord);
        OptionalRecord optionalRecord = (OptionalRecord) objectRecord;
        assertFalse(optionalRecord.isEmpty());
        assertNotNull(optionalRecord.getValue());
    }

    @Test
    void shouldHandleOptionalOfNull() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Optional<?> testOptional = Optional.ofNullable(null);

        recorder.write(testOptional, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Optional.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(OptionalRecord.class, objectRecord);
        OptionalRecord optionalRecord = (OptionalRecord) objectRecord;
        assertTrue(optionalRecord.isEmpty());
        assertNull(optionalRecord.getValue());
    }

    private static class CustomObject {
        private final String value;

        CustomObject(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "CustomObject{value='" + value + "'}";
        }
    }
} 