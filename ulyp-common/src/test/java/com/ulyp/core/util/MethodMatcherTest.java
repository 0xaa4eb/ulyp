package com.ulyp.core.util;

import com.ulyp.core.Method;
import com.ulyp.core.Type;
import org.junit.Test;

import static org.junit.Assert.*;

public class MethodMatcherTest {

    @Test
    public void testMatching() {

        Type type = Type.builder()
                .name("com.pckg.SomeClass")
                .build();

        Method method = Method.builder()
                .declaringType(type)
                .name("run")
                .build();

        assertTrue(MethodMatcher.parse("com.pckg.SomeClass.run").matches(method));

        assertTrue(MethodMatcher.parse("com.pckg.SomeClass.*").matches(method));

        assertTrue(MethodMatcher.parse("com.*.SomeClass.run").matches(method));

        assertTrue(MethodMatcher.parse("*.*").matches(method));

        assertTrue(MethodMatcher.parse("**.*").matches(method));

        assertTrue(MethodMatcher.parse("**.SomeClass.run").matches(method));
    }
}