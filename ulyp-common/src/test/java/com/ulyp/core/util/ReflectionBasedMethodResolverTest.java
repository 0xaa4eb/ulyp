package com.ulyp.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.ulyp.core.Method;
import com.ulyp.core.Type;

class ReflectionBasedMethodResolverTest {

    public static class T {

        public String get(Integer x) {
            return x.toString();
        }
    }

    @Test
    void testBasic() throws NoSuchMethodException {
        Method method = new ReflectionBasedMethodResolver().resolve(
            T.class.getDeclaredMethod("get", Integer.class)
        );

        Assertions.assertEquals(method.getName(), "get");
        Assertions.assertFalse(method.isStatic());

        Type declaringType = method.getDeclaringType();
        Assertions.assertEquals("com.ulyp.core.util.ReflectionBasedMethodResolverTest$T", declaringType.getName());
    }
}