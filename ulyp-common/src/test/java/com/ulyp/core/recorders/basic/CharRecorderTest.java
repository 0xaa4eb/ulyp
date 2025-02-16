package com.ulyp.core.recorders.basic;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CharRecorderTest {

    private final CharRecorder recorder = new CharRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldSupportCharTypes() {
        assertTrue(recorder.supports(Character.class));
        assertTrue(recorder.supports(char.class));
        
        assertFalse(recorder.supports(String.class));
        assertFalse(recorder.supports(Integer.class));
        assertFalse(recorder.supports(Object.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @ParameterizedTest
    @ValueSource(chars = {'a', 'Z', '0', '$', '\n', '\t', '\u0000', '\uffff'})
    void shouldRecordCharValues(char value) throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Character testValue = value;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Character.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(CharRecord.class, record);
        assertEquals(value, ((CharRecord) record).getValue());
        assertEquals(String.valueOf(value), record.toString());
    }

    @Test
    void shouldPreserveTypeInformation() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Character testValue = 'x';

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Character.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(Character.class.getName(), record.getType().getName());
    }

    @Test
    void shouldRecordMultipleValuesConsistently() throws Exception {
        BytesOut out1 = BytesOut.expandableArray();
        BytesOut out2 = BytesOut.expandableArray();
        
        recorder.write('a', out1, typeResolver);
        recorder.write('a', out2, typeResolver);

        ObjectRecord record1 = recorder.read(
            typeResolver.get(Character.class),
            out1.flip(),
            typeResolver::getById
        );
        ObjectRecord record2 = recorder.read(
            typeResolver.get(Character.class),
            out2.flip(),
            typeResolver::getById
        );

        assertEquals(record1.toString(), record2.toString());
        assertEquals(record1.getType(), record2.getType());
    }

    @Test
    void shouldHandleSpecialCharacters() throws Exception {
        char[] specialChars = {
            '\u0000',  // null character
            '\uffff',  // maximum unicode value
            '\n',      // newline
            '\r',      // carriage return
            '\t',      // tab
            '\b',      // backspace
            '\f',      // form feed
            '\'',      // single quote
            '\"',      // double quote
            '\\'       // backslash
        };

        for (char specialChar : specialChars) {
            BytesOut out = BytesOut.expandableArray();
            recorder.write(specialChar, out, typeResolver);

            ObjectRecord record = recorder.read(
                typeResolver.get(Character.class),
                out.flip(),
                typeResolver::getById
            );

            assertEquals(specialChar, ((CharRecord) record).getValue());
        }
    }

    @Test
    void shouldHandleNullValue() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Character testValue = null;

        assertThrows(NullPointerException.class, () -> recorder.write(testValue, out, typeResolver));
    }
} 