package com.ulyp.core.util;

import com.ulyp.core.Type;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypeMatcherTest {

    @Test
    public void testMatchingForNestedClass() {
        Type type = Type.builder().name("com.pckg.SomeClass$X").build();

        assertTrue(TypeMatcher.parse("**.X").matches(type));

        assertTrue(TypeMatcher.parse("**.SomeClass.X").matches(type));

        assertTrue(TypeMatcher.parse("**.SomeClass$X").matches(type));
    }

    @Test
    public void testMatchByWildcard() {
        Type type = Type.builder().name("com.pckg.SomeClass").build();

        assertTrue(TypeMatcher.parse("*").matches(type));
    }

    @Test
    public void testMatchingByFullName() {
        Type type = Type.builder().name("com.pckg.SomeClass").build();

        assertTrue(TypeMatcher.parse("com.pckg.SomeClass").matches(type));

        assertTrue(TypeMatcher.parse("com.*.SomeClass").matches(type));

        assertTrue(TypeMatcher.parse("**.SomeClass").matches(type));

        assertTrue(TypeMatcher.parse("com.*.So?eCla?s").matches(type));

        assertFalse(TypeMatcher.parse("COM.*.SomeClass").matches(type));

        assertFalse(TypeMatcher.parse("**.Some").matches(type));

        assertFalse(TypeMatcher.parse("com.pckg.SomeClasz").matches(type));
    }

    @Test
    public void testMatchingBySimpleName() {
        Type type = Type.builder().name("com.pckg.SomeClass").build();

        assertTrue(TypeMatcher.parse(">SomeClass").matches(type));
        assertTrue(TypeMatcher.parse(">someclass").matches(type));
        assertFalse(TypeMatcher.parse(">123").matches(type));
    }
}