package com.ulyp.core.recorders.arrays;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.numeric.IntegralRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntArrayRecorderTest {

    private final IntArrayRecorder recorder = new IntArrayRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @BeforeEach
    void setUp() {
        recorder.setEnabled(true);
        recorder.setMaxItemsToRecord(100); // Default max items
    }

    @Test
    void shouldRecordEmptyIntArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        int[] testArray = new int[0];

        recorder.write(testArray, out, typeResolver);

        ArrayRecord objectRecord = recorder.read(
            typeResolver.get(int[].class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(0, objectRecord.getLength());
        assertTrue(objectRecord.getElements().isEmpty());
    }

    @Test
    void shouldRecordSimpleIntArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        int[] testArray = {1, 2, 3, 4, 5};

        recorder.write(testArray, out, typeResolver);

        ArrayRecord objectRecord = recorder.read(
            typeResolver.get(int[].class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(5, objectRecord.getLength());
        assertEquals(5, objectRecord.getElements().size());
        
        for (int i = 0; i < testArray.length; i++) {
            IntegralRecord record = (IntegralRecord) objectRecord.getElements().get(i);
            assertEquals(testArray[i], record.getValue());
        }
    }

    @Test
    void shouldRespectMaxItemsToRecord() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        int[] testArray = new int[200];
        for (int i = 0; i < testArray.length; i++) {
            testArray[i] = i;
        }

        recorder.setMaxItemsToRecord(50);
        recorder.write(testArray, out, typeResolver);

        ArrayRecord objectRecord = recorder.read(
            typeResolver.get(int[].class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(200, objectRecord.getLength()); // Full length should be recorded
        assertEquals(50, objectRecord.getElements().size()); // But only max items recorded
        
        for (int i = 0; i < 50; i++) {
            IntegralRecord record = (IntegralRecord) objectRecord.getElements().get(i);
            assertEquals(i, record.getValue());
        }
    }

    @Test
    void shouldOnlySupportIntArrayClass() {
        assertTrue(recorder.supports(int[].class));
        assertFalse(recorder.supports(Object.class));
        assertFalse(recorder.supports(byte[].class));
        assertFalse(recorder.supports(Integer[].class));
        assertFalse(recorder.supports(int.class));
    }

    @Test
    void shouldNotSupportWhenDisabled() {
        recorder.setEnabled(false);
        assertFalse(recorder.supports(int[].class));
    }

    @Test
    void shouldSupportWhenEnabled() {
        recorder.setEnabled(true);
        assertTrue(recorder.supports(int[].class));
    }

    @Test
    void shouldHandleMaxIntegerValues() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        int[] testArray = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE};

        recorder.write(testArray, out, typeResolver);

        ArrayRecord objectRecord = recorder.read(
            typeResolver.get(int[].class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(5, objectRecord.getLength());
        
        IntegralRecord minRecord = (IntegralRecord) objectRecord.getElements().get(0);
        IntegralRecord maxRecord = (IntegralRecord) objectRecord.getElements().get(4);
        assertEquals(Integer.MIN_VALUE, minRecord.getValue());
        assertEquals(Integer.MAX_VALUE, maxRecord.getValue());
    }

    @Test
    void shouldHandleZeroMaxItemsToRecord() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        int[] testArray = {1, 2, 3, 4, 5};

        recorder.setMaxItemsToRecord(0);
        recorder.write(testArray, out, typeResolver);

        ArrayRecord objectRecord = recorder.read(
            typeResolver.get(int[].class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(5, objectRecord.getLength());
        assertTrue(objectRecord.getElements().isEmpty());
    }

    @Test
    void shouldHandleLargeMaxItemsToRecord() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        int[] testArray = {1, 2, 3};

        recorder.setMaxItemsToRecord(1000);
        recorder.write(testArray, out, typeResolver);

        ArrayRecord objectRecord = recorder.read(
            typeResolver.get(int[].class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(3, objectRecord.getLength());
        assertEquals(3, objectRecord.getElements().size()); // Should record all items when max is larger
    }

    @Test
    void shouldHandleDefaultInitializedArray() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        int[] testArray = new int[5]; // Will be initialized with zeros

        recorder.write(testArray, out, typeResolver);

        ArrayRecord objectRecord = recorder.read(
            typeResolver.get(int[].class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(5, objectRecord.getLength());
        
        for (Object element : objectRecord.getElements()) {
            IntegralRecord record = (IntegralRecord) element;
            assertEquals(0, record.getValue());
        }
    }
} 