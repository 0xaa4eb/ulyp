package com.ulyp.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void tokenizeToStringArray_ShouldHandleNullInput() {
        String[] result = StringUtils.tokenizeToStringArray(null, ",", true, true);
        
        assertNotNull(result, "Result should not be null for null input");
        assertEquals(0, result.length, "Empty array should be returned for null input");
    }

    @Test
    void tokenizeToStringArray_ShouldSplitString() {
        String input = "hello,world, test ";
        String[] result = StringUtils.tokenizeToStringArray(input, ",", true, true);
        
        assertArrayEquals(
            new String[]{"hello", "world", "test"},
            result,
            "String should be correctly split and trimmed"
        );
    }

    @Test
    void tokenizeToStringArray_ShouldRemoveEmptyTokens() {
        String input = "a,,b,,,c";
        String[] result = StringUtils.tokenizeToStringArray(input, ",", true, true);

        assertArrayEquals(
            new String[]{"a", "b", "c"},
            result,
            "Empty tokens should be kept when ignoreEmptyTokens is true"
        );
    }

    @ParameterizedTest
    @MethodSource("hasTextTestCases")
    void hasText_ShouldCorrectlyDetectText(String input, boolean expected, String message) {
        assertEquals(expected, StringUtils.hasText(input), message);
    }

    private static Stream<Arguments> hasTextTestCases() {
        return Stream.of(
            Arguments.of(null, false, "Null string should return false"),
            Arguments.of("", false, "Empty string should return false"),
            Arguments.of("   ", false, "Whitespace only string should return false"),
            Arguments.of("  a  ", true, "String with text should return true"),
            Arguments.of("abc", true, "Normal text should return true")
        );
    }

    @Test
    void toStringArray_ShouldConvertCollection() {
        List<String> input = Arrays.asList("a", "b", "c");
        String[] result = StringUtils.toStringArray(input);
        
        assertArrayEquals(
            new String[]{"a", "b", "c"},
            result,
            "Collection should be correctly converted to array"
        );
    }

    @Test
    void toStringArray_ShouldHandleEmptyCollection() {
        String[] result = StringUtils.toStringArray(Collections.emptyList());
        
        assertNotNull(result, "Result should not be null for empty collection");
        assertEquals(0, result.length, "Empty array should be returned for empty collection");
    }

    @ParameterizedTest
    @MethodSource("containsIgnoreCaseTestCases")
    void containsIgnoreCase_ShouldCorrectlyMatchStrings(
            String str, String searchStr, boolean expected, String message) {
        assertEquals(
            expected,
            StringUtils.containsIgnoreCase(str, searchStr),
            message
        );
    }

    private static Stream<Arguments> containsIgnoreCaseTestCases() {
        return Stream.of(
            Arguments.of(null, "test", false, "Null string should return false"),
            Arguments.of("test", null, false, "Null search string should return false"),
            Arguments.of("Hello World", "WORLD", true, "Case-insensitive match should work"),
            Arguments.of("Hello World", "universe", false, "Non-matching string should return false"),
            Arguments.of("HELLO", "hello", true, "Different case should match"),
            Arguments.of("", "", true, "Empty strings should match"),
            Arguments.of("abc", "abcd", false, "Longer search string should not match")
        );
    }

    @Test
    void regionMatches_ShouldMatchRegions() {
        assertTrue(
            StringUtils.regionMatches("Hello World", true, 6, "WORLD", 0, 5),
            "Case-insensitive region match should work"
        );
        
        assertFalse(
            StringUtils.regionMatches("Hello World", false, 6, "WORLD", 0, 5),
            "Case-sensitive region match should fail for different case"
        );
        
        assertTrue(
            StringUtils.regionMatches("Hello World", false, 6, "World", 0, 5),
            "Case-sensitive region match should work for same case"
        );
    }
} 