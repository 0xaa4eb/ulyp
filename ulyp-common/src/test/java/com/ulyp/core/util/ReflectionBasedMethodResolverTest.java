package com.ulyp.core.util;

import com.ulyp.core.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionBasedMethodResolverTest {

    private ReflectionBasedMethodResolver methodResolver;

    @BeforeEach
    void setUp() {
        methodResolver = new ReflectionBasedMethodResolver();
    }

    static class TestClass {
        public static void staticMethod() {}
        public String instanceMethod() { return "test"; }
        public void voidMethod() {}
        private void privateMethod() {}
    }

    @Test
    void shouldResolveStaticMethod() throws NoSuchMethodException {
        java.lang.reflect.Method staticMethod = TestClass.class.getDeclaredMethod("staticMethod");
        Method resolved = methodResolver.resolve(staticMethod);

        assertTrue(resolved.isStatic());
        assertFalse(resolved.isConstructor());
        assertFalse(resolved.returnsSomething());
        assertEquals("staticMethod", resolved.getName());
        assertEquals(TestClass.class.getName(), resolved.getType().getName());
    }

    @Test
    void shouldResolveInstanceMethodWithReturn() throws NoSuchMethodException {
        java.lang.reflect.Method instanceMethod = TestClass.class.getDeclaredMethod("instanceMethod");
        Method resolved = methodResolver.resolve(instanceMethod);

        assertFalse(resolved.isStatic());
        assertFalse(resolved.isConstructor());
        assertTrue(resolved.returnsSomething());
        assertEquals("instanceMethod", resolved.getName());
        assertEquals(TestClass.class.getName(), resolved.getType().getName());
    }

    @Test
    void shouldResolveVoidMethod() throws NoSuchMethodException {
        java.lang.reflect.Method voidMethod = TestClass.class.getDeclaredMethod("voidMethod");
        Method resolved = methodResolver.resolve(voidMethod);

        assertFalse(resolved.isStatic());
        assertFalse(resolved.isConstructor());
        assertFalse(resolved.returnsSomething());
        assertEquals("voidMethod", resolved.getName());
    }

    @Test
    void shouldResolvePrivateMethod() throws NoSuchMethodException {
        java.lang.reflect.Method privateMethod = TestClass.class.getDeclaredMethod("privateMethod");
        Method resolved = methodResolver.resolve(privateMethod);

        assertFalse(resolved.isStatic());
        assertFalse(resolved.isConstructor());
        assertFalse(resolved.returnsSomething());
        assertEquals("privateMethod", resolved.getName());
    }

    @Test
    void shouldGenerateUniqueIds() throws NoSuchMethodException {
        java.lang.reflect.Method method1 = TestClass.class.getDeclaredMethod("instanceMethod");
        java.lang.reflect.Method method2 = TestClass.class.getDeclaredMethod("voidMethod");

        Method resolved1 = methodResolver.resolve(method1);
        Method resolved2 = methodResolver.resolve(method2);

        assertNotEquals(resolved1.getId(), resolved2.getId());
    }
} 