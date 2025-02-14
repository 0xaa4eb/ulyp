package com.ulyp.core.recorders.basic;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class MethodRecorderTest {

    private final MethodRecorder recorder = new MethodRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    static class TestClass {
        public void publicMethod() {}
        private void privateMethod() {}
        protected void protectedMethod() {}
        void packagePrivateMethod() {}
        public static void staticMethod() {}
        public void methodWithParams(String s, int i) {}
        public <T> T genericMethod(T input) { return input; }
    }

    interface TestInterface {
        void interfaceMethod();
    }

    @Test
    void shouldSupportMethodType() {
        assertTrue(recorder.supports(Method.class));
        
        assertFalse(recorder.supports(String.class));
        assertFalse(recorder.supports(Object.class));
        assertFalse(recorder.supports(Class.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @Test
    void shouldRecordPublicMethod() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Method testValue = TestClass.class.getDeclaredMethod("publicMethod");

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Method.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(MethodRecord.class, record);
        assertEquals("publicMethod", ((MethodRecord) record).getName());
        assertEquals(TestClass.class.getName(), ((MethodRecord) record).getDeclaringType().getName());
    }

    @Test
    void shouldRecordPrivateMethod() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Method testValue = TestClass.class.getDeclaredMethod("privateMethod");

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Method.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals("privateMethod", ((MethodRecord) record).getName());
    }

    @Test
    void shouldRecordStaticMethod() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Method testValue = TestClass.class.getDeclaredMethod("staticMethod");

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Method.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals("staticMethod", ((MethodRecord) record).getName());
    }

    @Test
    void shouldRecordMethodWithParameters() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Method testValue = TestClass.class.getDeclaredMethod("methodWithParams", String.class, int.class);

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Method.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals("methodWithParams", ((MethodRecord) record).getName());
    }

    @Test
    void shouldRecordGenericMethod() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Method testValue = TestClass.class.getDeclaredMethod("genericMethod", Object.class);

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Method.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals("genericMethod", ((MethodRecord) record).getName());
    }

    @Test
    void shouldRecordInterfaceMethod() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Method testValue = TestInterface.class.getDeclaredMethod("interfaceMethod");

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(testValue),
            out.flip(),
            typeResolver::getById
        );

        assertEquals("interfaceMethod", ((MethodRecord) record).getName());
        assertEquals(TestInterface.class.getName(), ((MethodRecord) record).getDeclaringType().getName());
    }

    @Test
    void shouldPreserveTypeInformation() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Method testValue = TestClass.class.getDeclaredMethod("publicMethod");

        recorder.write(testValue, out, typeResolver);

        ObjectRecord record = recorder.read(
            typeResolver.get(Method.class),
            out.flip(),
            typeResolver::getById
        );

        assertEquals(Method.class.getName(), record.getType().getName());
    }

    @Test
    void shouldRecordMultipleValuesConsistently() throws Exception {
        BytesOut out1 = BytesOut.expandableArray();
        BytesOut out2 = BytesOut.expandableArray();
        Method testValue = TestClass.class.getDeclaredMethod("publicMethod");
        
        recorder.write(testValue, out1, typeResolver);
        recorder.write(testValue, out2, typeResolver);

        ObjectRecord record1 = recorder.read(
            typeResolver.get(Method.class),
            out1.flip(),
            typeResolver::getById
        );
        ObjectRecord record2 = recorder.read(
            typeResolver.get(Method.class),
            out2.flip(),
            typeResolver::getById
        );

        assertEquals(record1.toString(), record2.toString());
        assertEquals(record1.getType(), record2.getType());
    }

    @Test
    void shouldHandleNullValue() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Method testValue = null;

        assertThrows(NullPointerException.class, () -> recorder.write(testValue, out, typeResolver));
    }
} 