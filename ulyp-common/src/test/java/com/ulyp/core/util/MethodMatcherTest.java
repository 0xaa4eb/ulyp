package com.ulyp.core.util;

import com.ulyp.core.Method;
import com.ulyp.core.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MethodMatcherTest {

    @Test
    void testMatching() {

        Type type = Type.builder()
                .name("com.pckg.SomeClass")
                .build();

        Method method = Method.builder()
                .declaringType(type)
                .name("run")
                .build();

        Assertions.assertTrue(MethodMatcher.parse("com.pckg.SomeClass.run").matches(method));

        Assertions.assertTrue(MethodMatcher.parse("com.pckg.SomeClass.*").matches(method));

        Assertions.assertTrue(MethodMatcher.parse("com.*.SomeClass.run").matches(method));

        Assertions.assertTrue(MethodMatcher.parse("*.*").matches(method));

        Assertions.assertTrue(MethodMatcher.parse("**.*").matches(method));

        Assertions.assertTrue(MethodMatcher.parse("**.SomeClass.run").matches(method));
    }

    @Test
    void testMatchingWithNestedClass() {

        Type type = Type.builder()
            .name("com.pckg.SomeClass$Nested")
            .build();

        Method method = Method.builder()
            .declaringType(type)
            .name("run")
            .build();

        Assertions.assertTrue(MethodMatcher.parse("com.pckg.SomeClass$Nested.run").matches(method));

        Assertions.assertTrue(MethodMatcher.parse("com.pckg.SomeClass$Nested.*").matches(method));

        Assertions.assertTrue(MethodMatcher.parse("**.SomeClass$Nested.run").matches(method));
    }
}