package com.ulyp.core.util;

import java.util.Arrays;
import java.util.HashSet;

import com.ulyp.core.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TypeMatcherTest {

    @Test
    void testMatchingForNestedClass() {
        Type type = Type.builder().name("com.pckg.SomeClass$X").build();

        Assertions.assertTrue(TypeMatcher.parse("**.X").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("**.SomeClass.X").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("**.SomeClass$X").matches(type));
    }

    @Test
    void testMatchingByPackage() {
        Type type = Type.builder().name("com.org.pckg.SomeType").build();

        Assertions.assertTrue(TypeMatcher.parse("**.com.**").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("**.com.org.**").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("**.com.org.pckg.**").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("**.com.org.pckg.*").matches(type));
    }

    @Test
    void testMatchingForNestedClassForConvertedName() {
        Type type = Type.builder().name("com.pckg.SomeClass.X").build();

        Assertions.assertTrue(TypeMatcher.parse("**.X").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("**.SomeClass.X").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("**.pckg.SomeClass$X").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("**.SomeClass$X").matches(type));
    }

    @Test
    void testMatchByWildcard() {
        Type type = Type.builder().name("com.pckg.SomeClass").build();

        Assertions.assertTrue(TypeMatcher.parse("*").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("**").matches(type));
    }

    @Test
    void testMatchingByFullName() {
        Type type = Type.builder().name("com.pckg.SomeClass").build();

        Assertions.assertTrue(TypeMatcher.parse("com.pckg.SomeClass").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("com.*.SomeClass").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("**.SomeClass").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("com.*.So?eCla?s").matches(type));

        Assertions.assertFalse(TypeMatcher.parse("COM.*.SomeClass").matches(type));

        Assertions.assertFalse(TypeMatcher.parse("**.Some").matches(type));

        Assertions.assertFalse(TypeMatcher.parse("com.pckg.SomeClasz").matches(type));
    }

    @Test
    void testMatchBySuperType() {
        Type type = Type.builder()
            .name("com.pckg.SomeClass")
            .superTypeNames(new HashSet<>(Arrays.asList("com.test.SuperType")))
            .build();

        Assertions.assertTrue(TypeMatcher.parse("com.test.SuperType").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("**.test.SuperType").matches(type));

        Assertions.assertTrue(TypeMatcher.parse("**.SuperType").matches(type));
    }
}