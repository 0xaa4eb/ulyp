package com.ulyp.core.recorders.basic;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringRecorderTest {

    private final StringRecorder recorder = new StringRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldRecordStringValue() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        String testString = "Hello, World!";

        recorder.write(testString, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(String.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(StringObjectRecord.class, objectRecord);
        StringObjectRecord stringRecord = (StringObjectRecord) objectRecord;
        assertEquals(testString, stringRecord.value());
    }

    @Test
    void shouldHandleEmptyString() throws Exception {
    BytesOut out = BytesOut.expandableArray();
        String testString = "";

        recorder.write(testString, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(String.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(StringObjectRecord.class, objectRecord);
        StringObjectRecord stringRecord = (StringObjectRecord) objectRecord;
        assertEquals(testString, stringRecord.value());
    }

    @Test
    void shouldHandleSpecialCharacters() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        String testString = "Special chars: \n\t\r\f\"\\!@#$%^&*()";

        recorder.write(testString, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(String.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(StringObjectRecord.class, objectRecord);
        StringObjectRecord stringRecord = (StringObjectRecord) objectRecord;
        assertEquals(testString, stringRecord.value());
    }

    @Test
    void shouldOnlySupportStringClass() {
        assertTrue(recorder.supports(String.class));
        assertFalse(recorder.supports(Object.class));
        assertFalse(recorder.supports(Integer.class));
        assertFalse(recorder.supports(StringBuilder.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @Test
    void shouldHandleUnicodeCharacters() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        String testString = "Unicode: 你好世界 🌍 привет";

        recorder.write(testString, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(String.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(StringObjectRecord.class, objectRecord);
        StringObjectRecord stringRecord = (StringObjectRecord) objectRecord;
        assertEquals(testString, stringRecord.value());
    }
} 