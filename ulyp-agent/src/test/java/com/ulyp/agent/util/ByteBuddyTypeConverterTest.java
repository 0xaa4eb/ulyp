package com.ulyp.agent.util;

import java.util.HashSet;

import com.ulyp.core.Type;

import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ByteBuddyTypeConverterTest {

    private final ByteBuddyTypeConverter typeResolver = new ByteBuddyTypeConverter();

    @Test
    public void testNameResolve() {
        Type type = typeResolver.convert(TypeDescription.ForLoadedType.of(TestClass.class).asGenericType());

        Assertions.assertEquals("com.ulyp.agent.util.ByteBuddyTypeConverterTest$TestClass", type.getName());
    }

    @Test
    public void testBaseClassNamesResolve() {
        Type type = typeResolver.convert(TypeDescription.ForLoadedType.of(TestClass.class).asGenericType());

        Assertions.assertEquals(
            new HashSet<String>() {{
                add("com.ulyp.agent.util.ByteBuddyTypeConverterTest.BaseClass");
                add("com.ulyp.agent.util.ByteBuddyTypeConverterTest.I1");
                add("com.ulyp.agent.util.ByteBuddyTypeConverterTest.I2");
                add("com.ulyp.agent.util.ByteBuddyTypeConverterTest.I3");
                add("com.ulyp.agent.util.ByteBuddyTypeConverterTest.I4");
                add("com.ulyp.agent.util.ByteBuddyTypeConverterTest.I5");
            }},
            type.getSuperTypeNames()
        );
    }

    interface I1 {

    }

    interface I2 {

    }

    interface I5 {

    }

    interface I4 extends I5 {

    }

    interface I3 extends I4 {

    }

    static class BaseClass implements I2, I3 {

    }

    static class TestClass extends BaseClass implements I1 {

    }
}