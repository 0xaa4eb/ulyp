package com.ulyp.agent.util;

import com.ulyp.core.printers.TypeInfo;
import com.ulyp.core.printers.TypeTrait;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.*;

public class ByteBuddyTypeInfoTest {

    static class Y {

    }

    static class X {

        @Override
        public String toString() {
            return "X{}";
        }
    }

    @Test
    public void testHasToString() {

        assertTrue(ByteBuddyTypeInfo.of(TypeDescription.ForLoadedType.of(X.class).asGenericType()).hasToStringMethod());

        assertFalse(ByteBuddyTypeInfo.of(TypeDescription.ForLoadedType.of(Y.class).asGenericType()).hasToStringMethod());
    }

    static class BaseClass implements I2, I3 {

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

    static class TestClass extends BaseClass implements I1 {

    }

    public static <T> void takesGenericArray(T[] array) {

    }

    @Test
    public void testTakesGenericArray() throws NoSuchMethodException {
        MethodDescription.ForLoadedMethod.InDefinedShape.ForLoadedMethod method = new MethodDescription.ForLoadedMethod.InDefinedShape.ForLoadedMethod(
                this.getClass().getDeclaredMethod("takesGenericArray", Object[].class)
        );
        TypeDescription.Generic firstArgType = method.getParameters().asTypeList().get(0);


        TypeInfo byteBuddyTypeInfo = ByteBuddyTypeInfo.of(firstArgType);


        assertThat(byteBuddyTypeInfo.getTraits(), Matchers.hasItem(TypeTrait.NON_PRIMITIVE_ARRAY));
    }

    public static void takesObjectArray(Object[] array) {

    }

    @Test
    public void testTakesObjectArray() throws NoSuchMethodException {
        MethodDescription.ForLoadedMethod.InDefinedShape.ForLoadedMethod method = new MethodDescription.ForLoadedMethod.InDefinedShape.ForLoadedMethod(
                this.getClass().getDeclaredMethod("takesObjectArray", Object[].class)
        );
        TypeDescription.Generic firstArgType = method.getParameters().asTypeList().get(0);


        TypeInfo byteBuddyTypeInfo = ByteBuddyTypeInfo.of(firstArgType);


        assertThat(byteBuddyTypeInfo.getTraits(), Matchers.hasItem(TypeTrait.NON_PRIMITIVE_ARRAY));
    }

    public static void takesClass(Class<?> x) {

    }

    @Test
    public void testClassTypeTraits() throws NoSuchMethodException {
        MethodDescription.ForLoadedMethod.InDefinedShape.ForLoadedMethod method = new MethodDescription.ForLoadedMethod.InDefinedShape.ForLoadedMethod(
                this.getClass().getDeclaredMethod("takesClass", Class.class)
        );

        TypeDescription.Generic firstArgType = method.getParameters().asTypeList().get(0);

        TypeInfo type = ByteBuddyTypeInfo.of(firstArgType);

        assertThat(type.getTraits(), Matchers.hasItem(TypeTrait.CLASS_OBJECT));

        assertThat(type.getTraits(), Matchers.hasItem(TypeTrait.CONCRETE_CLASS));
    }

    @Test
    public void testNumberTypeTraits() {

        assertThat(ByteBuddyTypeInfo.of(Integer.class).getTraits(), Matchers.hasItem(TypeTrait.NUMBER));

        assertThat(ByteBuddyTypeInfo.of(Long.class).getTraits(), Matchers.hasItem(TypeTrait.NUMBER));
    }

    @Test
    public void testCollectionTraits() {

        assertThat(ByteBuddyTypeInfo.of(Collection.class).getTraits(), Matchers.hasItem(TypeTrait.COLLECTION));

        assertThat(ByteBuddyTypeInfo.of(Queue.class).getTraits(), Matchers.hasItem(TypeTrait.COLLECTION));

        assertThat(ByteBuddyTypeInfo.of(ConcurrentLinkedQueue.class).getTraits(), Matchers.hasItem(TypeTrait.COLLECTION));

        assertThat(ByteBuddyTypeInfo.of(List.class).getTraits(), Matchers.hasItem(TypeTrait.COLLECTION));

        assertThat(ByteBuddyTypeInfo.of(ArrayList.class).getTraits(), Matchers.hasItem(TypeTrait.COLLECTION));

        assertThat(ByteBuddyTypeInfo.of(Set.class).getTraits(), Matchers.hasItem(TypeTrait.COLLECTION));

        assertThat(ByteBuddyTypeInfo.of(HashSet.class).getTraits(), Matchers.hasItem(TypeTrait.COLLECTION));

        assertThat(ByteBuddyTypeInfo.of(CustomList.class).getTraits(), Matchers.hasItem(TypeTrait.COLLECTION));
    }

    public abstract class CustomList implements List<String> {

    }

    @Test
    public void testMapTraits() {

        assertThat(ByteBuddyTypeInfo.of(Map.class).getTraits(), Matchers.hasItem(TypeTrait.MAP));

        assertThat(ByteBuddyTypeInfo.of(HashMap.class).getTraits(), Matchers.hasItem(TypeTrait.MAP));

        assertThat(ByteBuddyTypeInfo.of(LinkedHashMap.class).getTraits(), Matchers.hasItem(TypeTrait.MAP));
    }

    @Test
    public void testBaseClassNamesResolve() {
        TypeInfo type = ByteBuddyTypeInfo.of(TestClass.class);

        Assert.assertEquals(
                new HashSet<String>() {{
                    add("com.ulyp.agent.util.ByteBuddyTypeInfoTest$TestClass");
                    add("com.ulyp.agent.util.ByteBuddyTypeInfoTest$BaseClass");
                }},
                type.getSuperClassesNames()
        );

        Assert.assertEquals(
                new HashSet<String>() {{
                    add("com.ulyp.agent.util.ByteBuddyTypeInfoTest$I1");
                    add("com.ulyp.agent.util.ByteBuddyTypeInfoTest$I2");
                    add("com.ulyp.agent.util.ByteBuddyTypeInfoTest$I3");
                    add("com.ulyp.agent.util.ByteBuddyTypeInfoTest$I4");
                    add("com.ulyp.agent.util.ByteBuddyTypeInfoTest$I5");
                }},
                type.getInterfacesClassesNames()
        );
    }
}