package com.ulyp.core.util;

import org.junit.Test;

import com.ulyp.core.Method;
import com.ulyp.core.Type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ReflectionBasedMethodResolverTest {

    public static class T {

        public String get(Integer x) {
            return x.toString();
        }
    }

    @Test
    public void testBasic() throws NoSuchMethodException {
        Method method = new ReflectionBasedMethodResolver().resolve(
            T.class.getDeclaredMethod("get", Integer.class)
        );

        assertEquals(method.getName(), "get");
        assertFalse(method.isStatic());

        Type declaringType = method.getDeclaringType();
        assertEquals("com.ulyp.core.util.ReflectionBasedMethodResolverTest$T", declaringType.getName());
    }
}