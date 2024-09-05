package com.ulyp.core.util;

import com.ulyp.core.Method;
import com.ulyp.core.Type;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodMatcherTest {

    @Test
    void testSimpleMatching() {

        Type type = Type.builder()
                .name("com.pckg.SomeClass")
                .build();

        Method method = Method.builder()
                .type(type)
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
    void testExcludeMatching() {

        MethodMatcher matcher = MethodMatcher.parse("**.Runner.run,-org.springframework.**.Runner.run");

        assertTrue(matcher.matches(
                Method.builder()
                        .type(Type.builder().name("com.framework.Runner").build())
                        .name("run")
                        .build()
        ));
        assertFalse(matcher.matches(
                Method.builder()
                        .type(Type.builder().name("org.springframework.Runner").build())
                        .name("run")
                        .build()
        ));
        assertFalse(matcher.matches(
                Method.builder()
                        .type(Type.builder().name("org.springframework.util.Runner").build())
                        .name("run")
                        .build()
        ));
    }

    @Test
    void testPrintingAndParsing() {
        MethodMatcher matcher = MethodMatcher.parse("**.A.foo,**.B.foo,**.B.bar,-**.A.bar");

        matcher = MethodMatcher.parse(matcher.toString());

        assertTrue(matcher.matches(
                Method.builder()
                        .type(Type.builder().name("com.test.A").build())
                        .name("foo")
                        .build()
        ));
        assertTrue(matcher.matches(
                Method.builder()
                        .type(Type.builder().name("com.test.B").build())
                        .name("foo")
                        .build()
        ));
        assertTrue(matcher.matches(
                Method.builder()
                        .type(Type.builder().name("com.test.B").build())
                        .name("bar")
                        .build()
        ));
        assertFalse(matcher.matches(
                Method.builder()
                        .type(Type.builder().name("com.test.A").build())
                        .name("bar")
                        .build()
        ));
    }

    @Test
    void testMatchingWithNestedClass() {

        Type type = Type.builder()
            .name("com.pckg.SomeClass$Nested")
            .build();

        Method method = Method.builder()
            .type(type)
            .name("run")
            .build();

        assertTrue(MethodMatcher.parse("com.pckg.SomeClass$Nested.run").matches(method));

        assertTrue(MethodMatcher.parse("com.pckg.SomeClass$Nested.*").matches(method));

        assertTrue(MethodMatcher.parse("**.SomeClass$Nested.run").matches(method));

        assertTrue(MethodMatcher.parse("**.Nested.run").matches(method));
    }

    @Test
    void testMatchingByPackage() {
        // Typical usage for recording all methods for certain package/framework
        MethodMatcher methodMatcher = MethodMatcher.parse("org.apache.kafka.**.*");

        assertTrue(methodMatcher.matches(
                Method.builder()
                        .type(Type.builder().name("org.apache.kafka.Producer").build())
                        .name("run")
                        .build())
        );
        assertTrue(methodMatcher.matches(
                Method.builder()
                        .type(Type.builder().name("org.apache.kafka.util.ByteUtils").build())
                        .name("computeOffset")
                        .build())
        );
        assertTrue(methodMatcher.matches(
                Method.builder()
                        .type(Type.builder().name("org.apache.kafka.Consumer").build())
                        .name("poll")
                        .build())
        );
        assertFalse(methodMatcher.matches(
                Method.builder()
                        .type(Type.builder().name("org.apache.ignite.Runner").build())
                        .name("run")
                        .build())
        );
    }
}