package com.ulyp.core.util;

import com.ulyp.core.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AntPatternTypeMatcherTest {

    @Test
    void shouldMatchExactClassName() {
        TypeMatcher matcher = AntPatternTypeMatcher.of("com.example.MyClass");
        Type type = mockType("com.example.MyClass", Collections.emptyList());

        assertTrue(matcher.matches(type), "Should match exact class name");
    }

    @Test
    void shouldMatchInnerClass() {
        TypeMatcher matcher = AntPatternTypeMatcher.of("com.example.Outer$Inner");
        Type type = mockType("com.example.Outer.Inner", Collections.emptyList());

        assertTrue(matcher.matches(type), "Should match inner class with $ replaced by .");
    }

    @Test
    void shouldMatchWildcard() {
        TypeMatcher matcher = AntPatternTypeMatcher.of("com.example.*");
        Type type = mockType("com.example.MyClass", Collections.emptyList());

        assertTrue(matcher.matches(type), "Should match wildcard pattern");
    }

    @Test
    void shouldMatchDoubleWildcard() {
        TypeMatcher matcher = AntPatternTypeMatcher.of("com.**.MyClass");
        Type type = mockType("com.example.sub.MyClass", Collections.emptyList());

        assertTrue(matcher.matches(type), "Should match double wildcard pattern");
    }

    @Test
    void shouldMatchSuperType() {
        TypeMatcher matcher = AntPatternTypeMatcher.of("com.example.SuperClass");
        Type type = mockType(
            "com.example.SubClass",
            Collections.singletonList("com.example.SuperClass")
        );

        assertTrue(matcher.matches(type), "Should match supertype");
    }

    @Test
    void shouldCreateSimpleMatcherForDoubleWildcardStart() {
        TypeMatcher matcher = AntPatternTypeMatcher.of("**.SimpleClass");
        Type type = mockType("com.example.SimpleClass", Collections.emptyList());

        assertTrue(matcher.matches(type), "Should match simple class name with ** prefix");
    }

    @ParameterizedTest(name = "Pattern {0} should not match {1}")
    @MethodSource("provideNonMatchingCases")
    void shouldNotMatch(String pattern, String typeName) {
        TypeMatcher matcher = AntPatternTypeMatcher.of(pattern);
        Type type = mockType(typeName, Collections.emptyList());

        assertFalse(matcher.matches(type), 
            String.format("Pattern '%s' should not match type '%s'", pattern, typeName));
    }

    private static Stream<Arguments> provideNonMatchingCases() {
        return Stream.of(
            Arguments.of("com.example.MyClass", "com.example.OtherClass"),
            Arguments.of("com.example.*", "com.other.MyClass"),
            Arguments.of("com.**.MyClass", "org.example.MyClass"),
            Arguments.of("**.SimpleClass", "com.example.ComplexClass")
        );
    }

    @Test
    void shouldReturnOriginalPatternInToString() {
        String pattern = "com.example.MyClass$Inner";
        TypeMatcher matcher = AntPatternTypeMatcher.of(pattern);

        assertEquals("com.example.MyClass.Inner", matcher.toString(), "toString should return original pattern");
    }

    private Type mockType(String name, List<String> superTypes) {
        return Type.builder()
                .name(name)
                .superTypeNames(new HashSet<>(superTypes))
                .build();
    }
} 