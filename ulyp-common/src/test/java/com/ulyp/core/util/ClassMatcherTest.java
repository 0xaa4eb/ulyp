package com.ulyp.core.util;

import com.ulyp.core.Type;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClassMatcherTest {

    @Test
    public void testMatchByWildcard() {
        Type type = Type.builder().name("com.pckg.SomeClass").build();

        assertTrue(ClassMatcher.parse("*").matches(type));
    }

    @Test
    public void testMatchingByFullName() {
        Type type = Type.builder().name("com.pckg.SomeClass").build();

        assertTrue(ClassMatcher.parse("com.pckg.SomeClass").matches(type));

        assertTrue(ClassMatcher.parse("com.*.SomeClass").matches(type));

        assertTrue(ClassMatcher.parse("**.SomeClass").matches(type));

        assertTrue(ClassMatcher.parse("com.*.So?eCla?s").matches(type));

        assertFalse(ClassMatcher.parse("COM.*.SomeClass").matches(type));

        assertFalse(ClassMatcher.parse("**.Some").matches(type));

        assertFalse(ClassMatcher.parse("com.pckg.SomeClasz").matches(type));
    }
}