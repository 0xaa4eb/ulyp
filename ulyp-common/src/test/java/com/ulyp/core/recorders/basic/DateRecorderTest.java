package com.ulyp.core.recorders.basic;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class DateRecorderTest {

    private final DateRecorder recorder = new DateRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldSupportDateType() {
        assertTrue(recorder.supports(Date.class));
        
        assertFalse(recorder.supports(Calendar.class));
        assertFalse(recorder.supports(String.class));
        assertFalse(recorder.supports(Object.class));
        assertFalse(recorder.supports(Long.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @Test
    void shouldRecordCurrentDate() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Date testValue = new Date();

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Date.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(DateRecord.class, record);
        assertEquals(testValue.toString(), record.toString());
    }

    @Test
    void shouldRecordSpecificDate() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(2024, Calendar.JANUARY, 1, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        BytesOut out = BytesOut.expandableArray();
        Date testValue = cal.getTime();

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Date.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(testValue.toString(), record.toString());
    }

    @Test
    void shouldRecordEpochDate() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Date testValue = new Date(0L); // epoch time

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Date.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(testValue.toString(), record.toString());
    }

    @Test
    void shouldPreserveTypeInformation() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Date testValue = new Date();

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Date.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(Date.class.getName(), record.getType().getName());
    }

    @Test
    void shouldRecordMultipleValuesConsistently() throws Exception {
        BytesOut out1 = BytesOut.expandableArray();
        BytesOut out2 = BytesOut.expandableArray();
        Date testValue = new Date(1000L);
        
        recorder.write(testValue, out1, typeResolver);
        recorder.write(testValue, out2, typeResolver);

        ObjectRecord record1 = recorder.read(
            typeResolver.get(Date.class),
            out1.flip(),
            typeResolver::getById
        );
        ObjectRecord record2 = recorder.read(
            typeResolver.get(Date.class),
            out2.flip(),
            typeResolver::getById
        );

        assertEquals(record1.toString(), record2.toString());
        assertEquals(record1.getType(), record2.getType());
    }

    @Test
    void shouldHandleNullValue() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Date testValue = null;

        assertThrows(NullPointerException.class, () -> recorder.write(testValue, out, typeResolver));
    }

    @Test
    void shouldHandleFutureDates() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Date testValue = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000); // one year in future

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Date.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(testValue.toString(), record.toString());
    }
} 