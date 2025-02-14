package com.ulyp.core.recorders.basic;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClassRecorderTest {

    private final ClassRecorder recorder = new ClassRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldSupportClassType() {
        assertTrue(recorder.supports(Class.class));
        
        assertFalse(recorder.supports(String.class));
        assertFalse(recorder.supports(Object.class));
        assertFalse(recorder.supports(Integer.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @Test
    void shouldRecordSimpleClasses() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Class<?> testValue = String.class;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Class.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ClassRecord.class, record);
        assertEquals(String.class.getName(), record.toString());
    }

    @Test
    void shouldRecordPrimitiveClasses() throws Exception {
        Class<?>[] primitives = {
            int.class,
            long.class,
            boolean.class,
            char.class,
            byte.class,
            short.class,
            float.class,
            double.class,
            void.class
        };

        for (Class<?> primitive : primitives) {
            BytesOut out = BytesOut.expandableArray();
            recorder.write(primitive, out, typeResolver);

            ObjectRecord record = recorder.read(
                typeResolver.get(Class.class),
                out.flip(),
                typeResolver::getById
            );

            assertEquals(primitive.getName(), record.toString());
        }
    }

    @Test
    void shouldRecordArrayClasses() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Class<?> testValue = int[].class;
        recorder.write(testValue, out, typeResolver);
        ObjectRecord record = recorder.read(
            typeResolver.get(Class.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals(int[].class.getName(), record.toString());

        out = BytesOut.expandableArray();
        testValue = String[][].class;
        recorder.write(testValue, out, typeResolver);
        record = recorder.read(
            typeResolver.get(Class.class),
            out.flip(),
            typeResolver::getById
        );
        assertEquals(String[][].class.getName(), record.toString());
    }

    @Test
    void shouldRecordInnerClasses() throws Exception {
        class LocalClass {}
        typeResolver.get(new LocalClass());
        
        BytesOut out = BytesOut.expandableArray();
        Class<?> testValue = LocalClass.class;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Class.class),
            out.flip(),
            typeResolver::getById
        );

        assertTrue(record.toString().contains("LocalClass"));
    }

    @Test
    void shouldRecordGenericClasses() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Class<?> testValue = ArrayList.class;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Class.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(ArrayList.class.getName(), record.toString());
    }

    @Test
    void shouldPreserveTypeInformation() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Class<?> testValue = Map.class;

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Class.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(Class.class.getName(), record.getType().getName());
    }

    @Test
    void shouldRecordMultipleValuesConsistently() throws Exception {
        BytesOut out1 = BytesOut.expandableArray();
        BytesOut out2 = BytesOut.expandableArray();
        
        recorder.write(String.class, out1, typeResolver);
        recorder.write(String.class, out2, typeResolver);

        ObjectRecord record1 = recorder.read(
            typeResolver.get(Class.class),
            out1.flip(),
            typeResolver::getById
        );
        ObjectRecord record2 = recorder.read(
            typeResolver.get(Class.class),
            out2.flip(),
            typeResolver::getById
        );

        assertEquals(record1.toString(), record2.toString());
        assertEquals(record1.getType(), record2.getType());
    }

    @Test
    void shouldHandleNullValue() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Class<?> testValue = null;

        assertThrows(NullPointerException.class, () -> recorder.write(testValue, out, typeResolver));
    }
} 