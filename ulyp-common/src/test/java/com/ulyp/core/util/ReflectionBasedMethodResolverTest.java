package com.ulyp.core.util;

import com.ulyp.core.Method;
import com.ulyp.core.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        Assertions.assertEquals("get", method.getName());
        Assertions.assertFalse(method.isStatic());

        Type declaringType = method.getType();
        Assertions.assertEquals("com.ulyp.core.util.ReflectionBasedMethodResolverTest$T", declaringType.getName());
    }
}