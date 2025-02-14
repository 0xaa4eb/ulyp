package com.ulyp.core.util;

import com.ulyp.core.Type;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

class SimpleNameTypeMatcherTest {

    @Test
    void shouldMatchExactSimpleName() {
        SimpleNameTypeMatcher matcher = new SimpleNameTypeMatcher("String");
        
        Type type = mock(Type.class);
        when(type.getName()).thenReturn("java.lang.String");
        
        assertTrue(matcher.matches(type));
    }

    @Test
    void shouldMatchCaseInsensitive() {
        SimpleNameTypeMatcher matcher = new SimpleNameTypeMatcher("string");
        
        Type type = mock(Type.class);
        when(type.getName()).thenReturn("java.lang.String");
        
        assertTrue(matcher.matches(type));
    }

    @Test
    void shouldNotMatchDifferentSimpleName() {
        SimpleNameTypeMatcher matcher = new SimpleNameTypeMatcher("Integer");
        
        Type type = mock(Type.class);
        when(type.getName()).thenReturn("java.lang.String");
        
        assertFalse(matcher.matches(type));
    }

    @Test
    void shouldMatchSuperTypeName() {
        SimpleNameTypeMatcher matcher = new SimpleNameTypeMatcher("List");
        
        Type type = mock(Type.class);
        when(type.getName()).thenReturn("java.util.ArrayList");
        when(type.getSuperTypeNames()).thenReturn(new HashSet<>(Arrays.asList(
            "java.util.List",
            "java.util.Collection"
        )));
        
        assertTrue(matcher.matches(type));
    }

    @Test
    void shouldMatchSuperTypeNameCaseInsensitive() {
        SimpleNameTypeMatcher matcher = new SimpleNameTypeMatcher("list");
        
        Type type = mock(Type.class);
        when(type.getName()).thenReturn("java.util.ArrayList");
        when(type.getSuperTypeNames()).thenReturn(new HashSet<>(Arrays.asList(
            "java.util.List",
            "java.util.Collection"
        )));
        
        assertTrue(matcher.matches(type));
    }

    @Test
    void testToString() {
        SimpleNameTypeMatcher matcher = new SimpleNameTypeMatcher("TestName");
        
        assertEquals("**.TestName", matcher.toString());
    }
} 