package com.ulyp.core.recorders.arrays;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharArrayRecorderTest {

    private final CharArrayRecorder recorder = new CharArrayRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @BeforeEach
    void setUp() {
        recorder.setEnabled(true);
    }

    @Test
    void shouldRecordEmptyCharArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        char[] testArray = new char[0];

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(char[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(CharArrayRecord.class, objectRecord);
        CharArrayRecord arrayRecord = (CharArrayRecord) objectRecord;
        assertEquals(0, arrayRecord.getLength());
    }

    @Test
    void shouldRecordSimpleCharArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        char[] testArray = {'H', 'e', 'l', 'l', 'o'};

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(char[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(CharArrayRecord.class, objectRecord);
        CharArrayRecord arrayRecord = (CharArrayRecord) objectRecord;
        assertEquals(5, arrayRecord.getLength());
    }

    @Test
    void shouldRecordUnicodeCharArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        char[] testArray = "Hello 你好 Привет 🌍".toCharArray();

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(char[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(CharArrayRecord.class, objectRecord);
        CharArrayRecord arrayRecord = (CharArrayRecord) objectRecord;
        assertEquals(testArray.length, arrayRecord.getLength());
    }

    @Test
    void shouldOnlySupportCharArrayClass() {
        assertTrue(recorder.supports(char[].class));
        assertFalse(recorder.supports(Object.class));
        assertFalse(recorder.supports(byte[].class));
        assertFalse(recorder.supports(Character[].class));
        assertFalse(recorder.supports(char.class));
    }

    @Test
    void shouldNotSupportWhenDisabled() {
        recorder.setEnabled(false);
        assertFalse(recorder.supports(char[].class));
    }

    @Test
    void shouldSupportWhenEnabled() {
        recorder.setEnabled(true);
        assertTrue(recorder.supports(char[].class));
    }

    @Test
    void shouldRecordArrayWithSpecialCharacters() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        char[] testArray = "\n\t\r\f\b\\\"\'".toCharArray();

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(char[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(CharArrayRecord.class, objectRecord);
        CharArrayRecord arrayRecord = (CharArrayRecord) objectRecord;
        assertEquals(testArray.length, arrayRecord.getLength());
    }

    @Test
    void shouldPreserveIdentityHashCode() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        char[] testArray = {'t', 'e', 's', 't'};

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(char[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(CharArrayRecord.class, objectRecord);
        CharArrayRecord arrayRecord = (CharArrayRecord) objectRecord;
        
        IdentityObjectRecord identityRecord = arrayRecord.getIdentityRecord();
        assertEquals(System.identityHashCode(testArray), identityRecord.getHashCode());
    }

    @Test
    void shouldHandleLargeCharArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        char[] testArray = new char[10000];
        for (int i = 0; i < testArray.length; i++) {
            testArray[i] = (char) (i % 65536); // Using modulo to keep within valid char range
        }

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(char[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(CharArrayRecord.class, objectRecord);
        CharArrayRecord arrayRecord = (CharArrayRecord) objectRecord;
        assertEquals(10000, arrayRecord.getLength());
    }

    @Test
    void shouldHandleNullChars() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        char[] testArray = new char[10]; // Will be initialized with null chars ('\u0000')

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(char[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(CharArrayRecord.class, objectRecord);
        CharArrayRecord arrayRecord = (CharArrayRecord) objectRecord;
        assertEquals(10, arrayRecord.getLength());
    }

    @Test
    void shouldHandleExtendedAsciiCharacters() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        char[] testArray = new char[256];
        for (int i = 0; i < 256; i++) {
            testArray[i] = (char) i;
        }

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(char[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(CharArrayRecord.class, objectRecord);
        CharArrayRecord arrayRecord = (CharArrayRecord) objectRecord;
        assertEquals(256, arrayRecord.getLength());
    }
} 