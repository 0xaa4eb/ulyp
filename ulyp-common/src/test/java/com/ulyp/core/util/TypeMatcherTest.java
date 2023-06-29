package com.ulyp.core.util;

import java.util.Arrays;
import java.util.HashSet;

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
    public void testMatchingByPackage() {
        Type type = Type.builder().name("com.org.pckg.SomeType").build();

        assertTrue(TypeMatcher.parse("**.com.**").matches(type));

        assertTrue(TypeMatcher.parse("**.com.org.**").matches(type));

        assertTrue(TypeMatcher.parse("**.com.org.pckg.**").matches(type));

        assertTrue(TypeMatcher.parse("**.com.org.pckg.*").matches(type));
    }

    @Test
    public void testMatchingForNestedClassForConvertedName() {
        Type type = Type.builder().name("com.pckg.SomeClass.X").build();

        assertTrue(TypeMatcher.parse("**.X").matches(type));

        assertTrue(TypeMatcher.parse("**.SomeClass.X").matches(type));

        assertTrue(TypeMatcher.parse("**.pckg.SomeClass$X").matches(type));

        assertTrue(TypeMatcher.parse("**.SomeClass$X").matches(type));
    }

    @Test
    public void testMatchByWildcard() {
        Type type = Type.builder().name("com.pckg.SomeClass").build();

        assertTrue(TypeMatcher.parse("*").matches(type));

        assertTrue(TypeMatcher.parse("**").matches(type));
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
    public void testMatchBySuperType() {
        Type type = Type.builder()
            .name("com.pckg.SomeClass")
            .superTypeNames(new HashSet<>(Arrays.asList("com.test.SuperType")))
            .build();

        assertTrue(TypeMatcher.parse("com.test.SuperType").matches(type));

        assertTrue(TypeMatcher.parse("**.test.SuperType").matches(type));

        assertTrue(TypeMatcher.parse("**.SuperType").matches(type));
    }
}