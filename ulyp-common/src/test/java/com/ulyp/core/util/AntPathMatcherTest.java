package com.ulyp.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntPathMatcherTest {

    private AntPathMatcher pathMatcher;

    @BeforeEach
    void setUp() {
        pathMatcher = new AntPathMatcher("/");
    }

    @ParameterizedTest(name = "pattern: {0}, path: {1}")
    @MethodSource("provideMatchingPaths")
    void shouldMatchValidPaths(String pattern, String path) {
        assertTrue(pathMatcher.match(pattern, path),
                () -> String.format("Pattern '%s' should match path '%s'", pattern, path));
    }

    @ParameterizedTest(name = "pattern: {0}, path: {1}")
    @MethodSource("provideNonMatchingPaths")
    void shouldNotMatchInvalidPaths(String pattern, String path) {
        assertFalse(pathMatcher.match(pattern, path),
                () -> String.format("Pattern '%s' should not match path '%s'", pattern, path));
    }

    @Test
    void shouldHandleNullPath() {
        assertFalse(pathMatcher.match("/test/**", null),
                "Pattern should not match null path");
    }

    private static Stream<Arguments> provideMatchingPaths() {
        return Stream.of(
            // Exact matches
            Arguments.of("/test", "/test"),
            Arguments.of("test", "test"),
            
            // Single wildcard matches
            Arguments.of("/test/*", "/test/file"),
            Arguments.of("/?est", "/test"),
            Arguments.of("/test/?", "/test/a"),
            
            // Double wildcard matches
            Arguments.of("/**", "/test"),
            Arguments.of("/**", "/test/file"),
            Arguments.of("/**", "/test/file/example"),
            Arguments.of("/test/**", "/test/file"),
            Arguments.of("/test/**", "/test/file/example"),
            Arguments.of("/**/test", "/a/b/test"),
            Arguments.of("/**/test", "/test"),
            
            // Complex pattern matches
            Arguments.of("/test/**/file", "/test/file"),
            Arguments.of("/test/**/file", "/test/a/file"),
            Arguments.of("/test/**/file", "/test/a/b/file"),
            Arguments.of("/**/*.jsp", "/folder/test.jsp"),
            Arguments.of("/**/???.???", "/folder/abc.txt"),
            
            // Pattern with multiple wildcards
            Arguments.of("/**/test/*.???", "/a/b/test/file.txt"),
            Arguments.of("/test/**/*.jsp", "/test/folder/file.jsp")
        );
    }

    private static Stream<Arguments> provideNonMatchingPaths() {
        return Stream.of(
            // Different paths
            Arguments.of("/test", "/test2"),
            Arguments.of("/test", "test"),
            
            // Wrong number of segments
            Arguments.of("/test/*", "/test/file/extra"),
            Arguments.of("/test/?", "/test/ab"),
            
            // Wrong file extensions
            Arguments.of("/**/*.jsp", "/folder/test.html"),
            Arguments.of("/**/???.???", "/folder/abcd.txt"),
            
            // Missing required segments
            Arguments.of("/test/**/file", "/test2/a/file"),
            Arguments.of("/**/test/*.jsp", "/test2/folder/file.jsp"),
            
            // Path separator mismatch
            Arguments.of("\\test", "/test"),
            
            // Case sensitivity
            Arguments.of("/TEST", "/test")
        );
    }
} 