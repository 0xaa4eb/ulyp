package com.ulyp.agent.util;

import com.ulyp.core.Type;
import com.ulyp.core.TypeTrait;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ByteBuddyTypeResolverTest {

    private final ByteBuddyTypeResolver typeResolver = new ByteBuddyTypeResolver();

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


        Type byteBuddyType = typeResolver.resolve(firstArgType);


        assertThat(byteBuddyType.getTraits(), hasItems(TypeTrait.NON_PRIMITIVE_ARRAY));
    }

    @Test
    public void testForChar() {

        Type byteBuddyType = typeResolver.get(char.class);

        assertThat(byteBuddyType.getTraits(), hasItem(TypeTrait.CHAR));
        assertThat(byteBuddyType.getTraits(), hasItem(TypeTrait.PRIMITIVE));

        byteBuddyType = typeResolver.get(Character.class);

        assertThat(byteBuddyType.getTraits(), hasItem(TypeTrait.CHAR));
        assertThat(byteBuddyType.getTraits(), not(hasItem(TypeTrait.PRIMITIVE)));
    }

    @Test
    public void testTraitsForPrimitiveArray() throws NoSuchMethodException {


        Type byteBuddyType = typeResolver.get(new int[]{45});


        assertThat(byteBuddyType.getTraits(), hasItem(TypeTrait.PRIMITIVE_ARRAY));
    }

    @Test
    public void testTraitsForObjectArray() throws NoSuchMethodException {


        Type byteBuddyType = typeResolver.get(new String[]{"A"});


        assertThat(byteBuddyType.getTraits(), hasItem(TypeTrait.NON_PRIMITIVE_ARRAY));
    }

    public static void takesObjectArray(Object[] array) {

    }

    @Test
    public void testObjectArrayTraitsWhenUsedAsParameter() throws NoSuchMethodException {
        MethodDescription.ForLoadedMethod.InDefinedShape.ForLoadedMethod method = new MethodDescription.ForLoadedMethod.InDefinedShape.ForLoadedMethod(
                this.getClass().getDeclaredMethod("takesObjectArray", Object[].class)
        );
        TypeDescription.Generic firstArgType = method.getParameters().asTypeList().get(0);


        Type byteBuddyType = typeResolver.resolve(firstArgType);


        assertThat(byteBuddyType.getTraits(), hasItem(TypeTrait.NON_PRIMITIVE_ARRAY));
    }

    public static void takesClass(Class<?> x) {

    }

    @Test
    public void testClassTypeTraits() throws NoSuchMethodException {
        MethodDescription.ForLoadedMethod.InDefinedShape.ForLoadedMethod method = new MethodDescription.ForLoadedMethod.InDefinedShape.ForLoadedMethod(
                this.getClass().getDeclaredMethod("takesClass", Class.class)
        );

        TypeDescription.Generic firstArgType = method.getParameters().asTypeList().get(0);

        Type type = typeResolver.resolve(firstArgType);

        assertThat(type.getTraits(), hasItem(TypeTrait.CLASS_OBJECT));

        assertThat(type.getTraits(), hasItem(TypeTrait.CONCRETE_CLASS));
    }

    @Test
    public void testNumberTypeTraits() {

        assertThat(typeResolver.get(Integer.class).getTraits(), hasItem(TypeTrait.NUMBER));

        assertThat(typeResolver.get(Long.class).getTraits(), hasItem(TypeTrait.NUMBER));
    }

    @Test
    public void testThrowableTraits() {

        assertThat(typeResolver.get(Throwable.class).getTraits(), hasItem(TypeTrait.THROWABLE));

        assertThat(typeResolver.get(VerifyError.class).getTraits(), hasItem(TypeTrait.THROWABLE));

        assertThat(typeResolver.get(Error.class).getTraits(), hasItem(TypeTrait.THROWABLE));

        assertThat(typeResolver.get(Exception.class).getTraits(), hasItem(TypeTrait.THROWABLE));

        assertThat(typeResolver.get(FileNotFoundException.class).getTraits(), hasItem(TypeTrait.THROWABLE));

        assertThat(typeResolver.get(RuntimeException.class).getTraits(), hasItem(TypeTrait.THROWABLE));
    }

    @Test
    public void testCollectionTraits() {

        assertThat(typeResolver.get(Collection.class).getTraits(), hasItem(TypeTrait.COLLECTION));

        assertThat(typeResolver.get(Queue.class).getTraits(), hasItem(TypeTrait.COLLECTION));

        assertThat(typeResolver.get(ConcurrentLinkedQueue.class).getTraits(), hasItem(TypeTrait.COLLECTION));

        assertThat(typeResolver.get(List.class).getTraits(), hasItem(TypeTrait.COLLECTION));

        assertThat(typeResolver.get(ArrayList.class).getTraits(), hasItem(TypeTrait.COLLECTION));

        assertThat(typeResolver.get(Set.class).getTraits(), hasItem(TypeTrait.COLLECTION));

        assertThat(typeResolver.get(HashSet.class).getTraits(), hasItem(TypeTrait.COLLECTION));

        assertThat(typeResolver.get(CustomList.class).getTraits(), hasItem(TypeTrait.COLLECTION));
    }

    public abstract class CustomList implements List<String> {

    }

    @Test
    public void testMapTraits() {

        assertThat(typeResolver.get(Map.class).getTraits(), hasItem(TypeTrait.MAP));

        assertThat(typeResolver.get(HashMap.class).getTraits(), hasItem(TypeTrait.MAP));

        assertThat(typeResolver.get(LinkedHashMap.class).getTraits(), hasItem(TypeTrait.MAP));
    }

    @Test
    public void testBaseClassNamesResolve() {
        Type type = typeResolver.get(TestClass.class);

        Assert.assertEquals(
                new HashSet<String>() {{
                    add("com.ulyp.agent.util.ByteBuddyTypeResolverTest.TestClass");
                    add("com.ulyp.agent.util.ByteBuddyTypeResolverTest.BaseClass");
                    add("com.ulyp.agent.util.ByteBuddyTypeResolverTest.I1");
                    add("com.ulyp.agent.util.ByteBuddyTypeResolverTest.I2");
                    add("com.ulyp.agent.util.ByteBuddyTypeResolverTest.I3");
                    add("com.ulyp.agent.util.ByteBuddyTypeResolverTest.I4");
                    add("com.ulyp.agent.util.ByteBuddyTypeResolverTest.I5");
                }},
                type.getSuperTypeNames()
        );

        Assert.assertEquals(
                new HashSet<String>() {{
                    add("BaseClass");
                    add("TestClass");
                    add("I1");
                    add("I2");
                    add("I3");
                    add("I4");
                    add("I5");
                }},
                type.getSuperTypeSimpleNames()
        );
    }
}