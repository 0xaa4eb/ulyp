package com.ulyp.core.recorders;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentityRecorderTest {

    private final IdentityRecorder recorder = new IdentityRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldRecordIdentityHashCode() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Object testObject = new Object();

        recorder.write(testObject, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Object.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(IdentityObjectRecord.class, objectRecord);
        IdentityObjectRecord identityRecord = (IdentityObjectRecord) objectRecord;
        assertEquals(System.identityHashCode(testObject), identityRecord.getHashCode());
    }

    @Test
    void shouldMaintainConsistentHashCodeForSameObject() throws Exception {
        BytesOut out1 = BytesOut.expandableArray();
        BytesOut out2 = BytesOut.expandableArray();
        Object testObject = new Object();

        recorder.write(testObject, out1, typeResolver);
        recorder.write(testObject, out2, typeResolver);

        ObjectRecord record1 = recorder.read(
            typeResolver.get(Object.class),
            out1.flip(),
            typeResolver::getById
        );
        ObjectRecord record2 = recorder.read(
            typeResolver.get(Object.class),
            out2.flip(),
            typeResolver::getById
        );

        IdentityObjectRecord identityRecord1 = (IdentityObjectRecord) record1;
        IdentityObjectRecord identityRecord2 = (IdentityObjectRecord) record2;
        assertEquals(identityRecord1.getHashCode(), identityRecord2.getHashCode());
    }

    @Test
    void shouldSupportAllTypes() throws Exception {
        assertTrue(recorder.supports(Object.class));
        assertTrue(recorder.supports(String.class));
        assertTrue(recorder.supports(Integer.class));
        assertTrue(recorder.supports(CustomClass.class));
    }

    @Test
    void shouldSupportAsyncRecording() throws Exception {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @Test
    void shouldRecordDifferentHashCodesForDifferentObjects() throws Exception {
        BytesOut out1 = BytesOut.expandableArray();
        BytesOut out2 = BytesOut.expandableArray();
        Object testObject1 = new Object();
        Object testObject2 = new Object();

        recorder.write(testObject1, out1, typeResolver);
        recorder.write(testObject2, out2, typeResolver);

        ObjectRecord record1 = recorder.read(
            typeResolver.get(Object.class),
            out1.flip(),
            typeResolver::getById
        );
        ObjectRecord record2 = recorder.read(
            typeResolver.get(Object.class),
            out2.flip(),
            typeResolver::getById
        );

        assertNotEquals(record1, record2);
    }

    private static class CustomClass {
        private final String value;

        CustomClass(String value) {
            this.value = value;
        }
    }

    @Test
    void shouldHandleCustomObjects() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        CustomClass testObject = new CustomClass("test");

        recorder.write(testObject, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(CustomClass.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(IdentityObjectRecord.class, objectRecord);
        IdentityObjectRecord identityRecord = (IdentityObjectRecord) objectRecord;
        assertEquals(System.identityHashCode(testObject), identityRecord.getHashCode());
    }
} 