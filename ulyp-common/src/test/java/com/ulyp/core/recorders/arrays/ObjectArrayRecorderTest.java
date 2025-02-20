package com.ulyp.core.recorders.arrays;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.basic.NullObjectRecord;
import com.ulyp.core.recorders.basic.StringObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectArrayRecorderTest {

    private final ObjectArrayRecorder recorder = new ObjectArrayRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @BeforeEach
    void setUp() {
        // TODO this is kind of bad, need to decouple those recorders from static layer
        ObjectArrayRecorder arrayRecorder = (ObjectArrayRecorder) ObjectRecorderRegistry.OBJECT_ARRAY_RECORDER.getInstance();
        arrayRecorder.setEnabled(true);

        recorder.setEnabled(true);
        recorder.setMaxItemsToRecord(100); // Default max items
    }

    @Test
    void shouldRecordEmptyObjectArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Object[] testArray = new Object[0];

        recorder.write(testArray, out, typeResolver);

        ArrayRecord objectRecord = recorder.read(
            typeResolver.get(Object[].class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(0, objectRecord.getLength());
        assertTrue(objectRecord.getElements().isEmpty());
    }

    @Test
    void shouldRecordStringArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        String[] testArray = {"Hello", "World", "Test"};

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(String[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ArrayRecord.class, objectRecord);
        ArrayRecord arrayRecord = (ArrayRecord) objectRecord;
        assertEquals(3, arrayRecord.getLength());
        assertEquals(3, arrayRecord.getElements().size());
        
        for (int i = 0; i < testArray.length; i++) {
            StringObjectRecord record = (StringObjectRecord) arrayRecord.getElements().get(i);
            assertEquals(testArray[i], record.value());
        }
    }

    @Test
    void shouldRespectMaxItemsToRecord() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        String[] testArray = new String[200];
        for (int i = 0; i < testArray.length; i++) {
            testArray[i] = "Item" + i;
        }

        recorder.setMaxItemsToRecord(50);
        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(String[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ArrayRecord.class, objectRecord);
        ArrayRecord arrayRecord = (ArrayRecord) objectRecord;
        assertEquals(200, arrayRecord.getLength()); // Full length should be recorded
        assertEquals(50, arrayRecord.getElements().size()); // But only max items recorded
        
        for (int i = 0; i < 50; i++) {
            StringObjectRecord record = (StringObjectRecord) arrayRecord.getElements().get(i);
            assertEquals("Item" + i, record.value());
        }
    }

    @Test
    void shouldOnlySupportObjectArrayClasses() {
        assertTrue(recorder.supports(Object[].class));
        assertTrue(recorder.supports(String[].class));
        assertTrue(recorder.supports(CustomObject[].class));
        
        assertFalse(recorder.supports(Object.class));
        assertFalse(recorder.supports(int[].class));
        assertFalse(recorder.supports(byte[].class));
    }

    @Test
    void shouldNotSupportWhenDisabled() {
        recorder.setEnabled(false);
        assertFalse(recorder.supports(Object[].class));
    }

    @Test
    void shouldHandleArrayWithNullElements() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        String[] testArray = new String[5]; // All elements are null

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(String[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ArrayRecord.class, objectRecord);
        ArrayRecord arrayRecord = (ArrayRecord) objectRecord;
        assertEquals(5, arrayRecord.getLength());
        assertEquals(5, arrayRecord.getElements().size());
        
        for (ObjectRecord element : arrayRecord.getElements()) {
            assertInstanceOf(NullObjectRecord.class, element);
        }
    }

    @Test
    void shouldHandleMixedNullAndNonNullElements() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        String[] testArray = new String[]{"First", null, "Third", null, "Fifth"};

        recorder.write(testArray, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(String[].class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ArrayRecord.class, objectRecord);
        ArrayRecord arrayRecord = (ArrayRecord) objectRecord;
        assertEquals(5, arrayRecord.getLength());
        assertEquals(5, arrayRecord.getElements().size());
        
        assertInstanceOf(StringObjectRecord.class, arrayRecord.getElements().get(0));
        assertInstanceOf(NullObjectRecord.class, arrayRecord.getElements().get(1));
        assertInstanceOf(StringObjectRecord.class, arrayRecord.getElements().get(2));
        assertInstanceOf(NullObjectRecord.class, arrayRecord.getElements().get(3));
        assertInstanceOf(StringObjectRecord.class, arrayRecord.getElements().get(4));
    }

    @Test
    void shouldHandleCustomObjectArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        CustomObject[] testArray = {
            new CustomObject("First"),
            new CustomObject("Second"),
            new CustomObject("Third")
        };

        recorder.write(testArray, out, typeResolver);

        ArrayRecord objectRecord = recorder.read(
            typeResolver.get(CustomObject[].class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(3, objectRecord.getLength());
        assertEquals(3, objectRecord.getElements().size());
    }

    @Test
    void shouldHandleNestedArrays() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Object[] testArray = {
            new String[]{"Nested", "Array", "1"},
            new Integer[]{1, 2, 3},
            new CustomObject[]{ new CustomObject("Nested") }
        };

        recorder.write(testArray, out, typeResolver);

        ArrayRecord objectRecord = recorder.read(
            typeResolver.get(Object[].class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(3, objectRecord.getLength());
        assertEquals(3, objectRecord.getElements().size());
        
        // Each element should be an ArrayRecord
        for (ObjectRecord element : objectRecord.getElements()) {
            assertInstanceOf(ArrayRecord.class, element);
        }
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