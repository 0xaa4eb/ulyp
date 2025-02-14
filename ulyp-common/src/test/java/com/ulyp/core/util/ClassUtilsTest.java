package com.ulyp.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassUtilsTest {

    @ParameterizedTest(name = "Should extract simple name '{1}' from '{0}'")
    @MethodSource("provideClassNameTestCases")
    void shouldExtractSimpleName(String fullName, String expectedSimpleName) {
        assertEquals(expectedSimpleName, ClassUtils.getSimpleNameFromName(fullName),
            "Failed to extract correct simple name from: " + fullName);
    }

    private static Stream<Arguments> provideClassNameTestCases() {
        return Stream.of(
            // Simple class names
            Arguments.of("SimpleClass", "SimpleClass"),
            Arguments.of("Test", "Test"),
            
            // Classes with package names
            Arguments.of("com.example.TestClass", "TestClass"),
            Arguments.of("very.long.package.name.MyClass", "MyClass"),
            Arguments.of("a.b.c.D", "D"),
            
            // Inner classes with $ notation
            Arguments.of("com.example.OuterClass$InnerClass", "InnerClass"),
            Arguments.of("OuterClass$InnerClass", "InnerClass"),
            Arguments.of("com.example.Outer$Inner$NestedInner", "NestedInner"),
            
            // Edge cases
            Arguments.of("NoPackage", "NoPackage"),
            
            // Multiple dots and dollar signs
            Arguments.of("com.example.test.Class$Inner$More", "More"),
            Arguments.of("a.b.c.d.e.f.g.Class", "Class"),
            
            // Single character names
            Arguments.of("com.example.A", "A"),
            Arguments.of("x.y.z.Z$A", "A")
        );
    }

    @Test
    void shouldHandleDotAtEnd() {
        assertEquals("", ClassUtils.getSimpleNameFromName("com.example."),
            "Should handle dot at the end of the string");
    }

    @Test
    void shouldHandleDollarAtEnd() {
        assertEquals("", ClassUtils.getSimpleNameFromName("com.example.Class$"),
            "Should handle dollar sign at the end of the string");
    }

    @Test
    void shouldHandleMultipleConsecutiveSeparators() {
        assertEquals("Class", ClassUtils.getSimpleNameFromName("com..example...Class"),
            "Should handle multiple consecutive dots");
        assertEquals("Inner", ClassUtils.getSimpleNameFromName("Outer$$Inner"),
            "Should handle multiple consecutive dollar signs");
    }
} 