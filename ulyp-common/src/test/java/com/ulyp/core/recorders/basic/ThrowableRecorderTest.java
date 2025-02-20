package com.ulyp.core.recorders.basic;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThrowableRecorderTest {

    private final ThrowableRecorder recorder = new ThrowableRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldRecordExceptionWithMessage() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Exception testException = new RuntimeException("Test error message");

        recorder.write(testException, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(RuntimeException.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ThrowableRecord.class, objectRecord);
        ThrowableRecord throwableRecord = (ThrowableRecord) objectRecord;
        assertInstanceOf(StringObjectRecord.class, throwableRecord.getMessage());
        assertEquals("Test error message", ((StringObjectRecord) throwableRecord.getMessage()).value());
    }

    @Test
    void shouldRecordExceptionWithNullMessage() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Exception testException = new RuntimeException();

        recorder.write(testException, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(RuntimeException.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ThrowableRecord.class, objectRecord);
        ThrowableRecord throwableRecord = (ThrowableRecord) objectRecord;
        assertInstanceOf(NullObjectRecord.class, throwableRecord.getMessage());
    }

    @Test
    void shouldSupportAllThrowableTypes() {
        assertTrue(recorder.supports(Throwable.class));
        assertTrue(recorder.supports(Exception.class));
        assertTrue(recorder.supports(RuntimeException.class));
        assertTrue(recorder.supports(Error.class));
        assertTrue(recorder.supports(CustomException.class));
        
        assertFalse(recorder.supports(String.class));
        assertFalse(recorder.supports(Object.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @Test
    void shouldRecordCustomException() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        CustomException testException = new CustomException("Custom error");

        recorder.write(testException, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(CustomException.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ThrowableRecord.class, objectRecord);
        ThrowableRecord throwableRecord = (ThrowableRecord) objectRecord;
        assertInstanceOf(StringObjectRecord.class, throwableRecord.getMessage());
        assertEquals("Custom error", ((StringObjectRecord) throwableRecord.getMessage()).value());
    }

    @Test
    void shouldRecordExceptionWithSpecialCharacters() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Exception testException = new RuntimeException("Error with special chars: \n\t\r\"\\!@#$%^&*()");

        recorder.write(testException, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(RuntimeException.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ThrowableRecord.class, objectRecord);
        ThrowableRecord throwableRecord = (ThrowableRecord) objectRecord;
        assertInstanceOf(StringObjectRecord.class, throwableRecord.getMessage());
        assertEquals("Error with special chars: \n\t\r\"\\!@#$%^&*()", 
            ((StringObjectRecord) throwableRecord.getMessage()).value());
    }

    @Test
    void shouldRecordExceptionWithUnicodeMessage() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Exception testException = new RuntimeException("错误 🔥 ошибка");

        recorder.write(testException, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(RuntimeException.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ThrowableRecord.class, objectRecord);
        ThrowableRecord throwableRecord = (ThrowableRecord) objectRecord;
        assertInstanceOf(StringObjectRecord.class, throwableRecord.getMessage());
        assertEquals("错误 🔥 ошибка", ((StringObjectRecord) throwableRecord.getMessage()).value());
    }

    @Test
    void shouldHandleErrorClass() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Error testError = new OutOfMemoryError("Out of memory");

        recorder.write(testError, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(OutOfMemoryError.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ThrowableRecord.class, objectRecord);
        ThrowableRecord throwableRecord = (ThrowableRecord) objectRecord;
        assertInstanceOf(StringObjectRecord.class, throwableRecord.getMessage());
        assertEquals("Out of memory", ((StringObjectRecord) throwableRecord.getMessage()).value());
    }

    private static class CustomException extends Exception {
        public CustomException(String message) {
            super(message);
        }
    }
} 