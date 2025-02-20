package com.ulyp.core.recorders.arrays;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ByteArrayRecorderTest {

    private final ByteArrayRecorder recorder = new ByteArrayRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @BeforeEach
    void setUp() {
        recorder.setEnabled(true);
    }

    @Test
    void shouldRecordEmptyByteArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        byte[] testArray = new byte[0];

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(byte[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ByteArrayRecord.class, objectRecord);
        ByteArrayRecord arrayRecord = (ByteArrayRecord) objectRecord;
        assertEquals(0, arrayRecord.getLength());
    }

    @Test
    void shouldRecordSimpleByteArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        byte[] testArray = {1, 2, 3, 4, 5};

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(byte[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ByteArrayRecord.class, objectRecord);
        ByteArrayRecord arrayRecord = (ByteArrayRecord) objectRecord;
        assertEquals(5, arrayRecord.getLength());
    }

    @Test
    void shouldRecordLargeByteArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        byte[] testArray = new byte[10000];
        new Random().nextBytes(testArray);

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(byte[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ByteArrayRecord.class, objectRecord);
        ByteArrayRecord arrayRecord = (ByteArrayRecord) objectRecord;
        assertEquals(10000, arrayRecord.getLength());
    }

    @Test
    void shouldOnlySupportByteArrayClass() {
        assertTrue(recorder.supports(byte[].class));
        assertFalse(recorder.supports(Object.class));
        assertFalse(recorder.supports(int[].class));
        assertFalse(recorder.supports(Byte[].class));
        assertFalse(recorder.supports(byte.class));
    }

    @Test
    void shouldNotSupportWhenDisabled() {
        recorder.setEnabled(false);
        assertFalse(recorder.supports(byte[].class));
    }

    @Test
    void shouldSupportWhenEnabled() {
        recorder.setEnabled(true);
        assertTrue(recorder.supports(byte[].class));
    }

    @Test
    void shouldRecordArrayWithAllPossibleByteValues() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        byte[] testArray = new byte[256];
        for (int i = 0; i < 256; i++) {
            testArray[i] = (byte) (i - 128);
        }

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(byte[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ByteArrayRecord.class, objectRecord);
        ByteArrayRecord arrayRecord = (ByteArrayRecord) objectRecord;
        assertEquals(256, arrayRecord.getLength());
    }

    @Test
    void shouldPreserveIdentityHashCode() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        byte[] testArray = {1, 2, 3};

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(byte[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ByteArrayRecord.class, objectRecord);
        ByteArrayRecord arrayRecord = (ByteArrayRecord) objectRecord;
        
        IdentityObjectRecord identityRecord = arrayRecord.getIdentityRecord();
        assertEquals(System.identityHashCode(testArray), identityRecord.getHashCode());
    }

    @Test
    void shouldHandleMaxSizeArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        byte[] testArray = new byte[1024 * 1024];

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(byte[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ByteArrayRecord.class, objectRecord);
        ByteArrayRecord arrayRecord = (ByteArrayRecord) objectRecord;
        assertEquals(1024 * 1024, arrayRecord.getLength());
    }

    @Test
    void shouldHandleNullBytes() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        byte[] testArray = new byte[10];

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(byte[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ByteArrayRecord.class, objectRecord);
        ByteArrayRecord arrayRecord = (ByteArrayRecord) objectRecord;
        assertEquals(10, arrayRecord.getLength());
    }
} 