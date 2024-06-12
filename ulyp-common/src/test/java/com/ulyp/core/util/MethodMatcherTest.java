package com.ulyp.core.util;

import com.ulyp.core.Method;
import com.ulyp.core.Type;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodMatcherTest {

    @Test
    void testSimpleMatching() {

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

    @Test
    void testMatchingWithNestedClass() {

        Type type = Type.builder()
            .name("com.pckg.SomeClass$Nested")
            .build();

        Method method = Method.builder()
            .declaringType(type)
            .name("run")
            .build();

        assertTrue(MethodMatcher.parse("com.pckg.SomeClass$Nested.run").matches(method));

        assertTrue(MethodMatcher.parse("com.pckg.SomeClass$Nested.*").matches(method));

        assertTrue(MethodMatcher.parse("**.SomeClass$Nested.run").matches(method));

        assertTrue(MethodMatcher.parse("**.Nested.run").matches(method));
    }
}