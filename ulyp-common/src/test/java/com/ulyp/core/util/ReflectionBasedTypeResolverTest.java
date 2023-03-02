package com.ulyp.core.util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.ulyp.core.Type;
import com.ulyp.core.TypeTrait;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class ReflectionBasedTypeResolverTest {

    private final ReflectionBasedTypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    public void testTakesGenericArray() throws NoSuchMethodException {
        Type byteBuddyType = typeResolver.get(Object[].class);

        assertThat(byteBuddyType.getTraits(), hasItems(TypeTrait.NON_PRIMITIVE_ARRAY));
    }

    @Test
    public void testEnumTrait() {

        Type type = typeResolver.get(TestEnum.class);

        assertThat(type.getTraits(), hasItem(TypeTrait.ENUM));
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
    public void testTraitsForPrimitiveArray() {


        Type byteBuddyType = typeResolver.get(new byte[]{45});


        assertThat(byteBuddyType.getTraits(), hasItem(TypeTrait.PRIMITIVE_BYTE_ARRAY));
    }

    @Test
    public void testTraitsForObjectArray() {


        Type byteBuddyType = typeResolver.get(new String[]{"A"});


        assertThat(byteBuddyType.getTraits(), hasItem(TypeTrait.NON_PRIMITIVE_ARRAY));
    }

    @Test
    @Ignore
    public void testObjectArrayTraitsWhenUsedAsParameter() throws NoSuchMethodException {
        Type byteBuddyType = typeResolver.get(int[].class);


        assertThat(byteBuddyType.getTraits(), hasItem(TypeTrait.NON_PRIMITIVE_ARRAY));
    }

    @Test
    public void testConcreteClassTrait() {
        assertThat(typeResolver.get(CustomAbstractList.class).getTraits(), not(hasItem(TypeTrait.CONCRETE_CLASS)));

        assertThat(typeResolver.get(I3.class).getTraits(), not(hasItem(TypeTrait.CONCRETE_CLASS)));

        assertThat(typeResolver.get(ArrayList.class).getTraits(), hasItem(TypeTrait.CONCRETE_CLASS));
    }

    @Test
    public void testClassTypeTraits() throws NoSuchMethodException {
        Type type = typeResolver.get(Class.class);

        assertThat(type.getTraits(), hasItem(TypeTrait.CLASS_OBJECT));
    }

    @Test
    public void testIntegral() {

        assertThat(typeResolver.get(Integer.class).getTraits(), hasItem(TypeTrait.INTEGRAL));

        assertThat(typeResolver.get(Short.class).getTraits(), hasItem(TypeTrait.INTEGRAL));

        assertThat(typeResolver.get(Long.class).getTraits(), hasItem(TypeTrait.INTEGRAL));

        assertThat(typeResolver.get(int.class).getTraits(), hasItem(TypeTrait.INTEGRAL));

        assertThat(typeResolver.get(short.class).getTraits(), hasItem(TypeTrait.INTEGRAL));

        assertThat(typeResolver.get(long.class).getTraits(), hasItem(TypeTrait.INTEGRAL));
    }

    @Test
    public void testNumberTypeTraits() {

        assertThat(typeResolver.get(Integer.class).getTraits(), hasItem(TypeTrait.NUMBER));

        assertThat(typeResolver.get(Short.class).getTraits(), hasItem(TypeTrait.NUMBER));

        assertThat(typeResolver.get(Long.class).getTraits(), hasItem(TypeTrait.NUMBER));
    }

    @Test
    public void testThrowableTraits() {

        assertThat(typeResolver.get(Throwable.class).getTraits(), hasItem(TypeTrait.THROWABLE));
        assertThat(typeResolver.get(new Throwable()).getTraits(), hasItem(TypeTrait.THROWABLE));

        assertThat(typeResolver.get(VerifyError.class).getTraits(), hasItem(TypeTrait.THROWABLE));
        assertThat(typeResolver.get(new VerifyError()).getTraits(), hasItem(TypeTrait.THROWABLE));

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
        assertThat(typeResolver.get(new ArrayList<String>()).getTraits(), hasItem(TypeTrait.COLLECTION));

        assertThat(typeResolver.get(Set.class).getTraits(), hasItem(TypeTrait.COLLECTION));

        assertThat(typeResolver.get(new HashSet<String>()).getTraits(), hasItem(TypeTrait.COLLECTION));
        assertThat(typeResolver.get(HashSet.class).getTraits(), hasItem(TypeTrait.COLLECTION));

        assertThat(typeResolver.get(CustomAbstractList.class).getTraits(), hasItem(TypeTrait.COLLECTION));
    }

    @Test
    public void testBoolTrait() {

        assertThat(typeResolver.get(Boolean.class).getTraits(), hasItem(TypeTrait.BOOLEAN));

        assertThat(typeResolver.get(boolean.class).getTraits(), hasItem(TypeTrait.BOOLEAN));
    }

    @Test
    public void testCharTraits() {

        Type byteBuddyType = typeResolver.get(char.class);

        assertThat(byteBuddyType.getTraits(), hasItem(TypeTrait.CHAR));
        assertThat(byteBuddyType.getTraits(), hasItem(TypeTrait.PRIMITIVE));

        byteBuddyType = typeResolver.get(Character.class);

        assertThat(byteBuddyType.getTraits(), hasItem(TypeTrait.CHAR));
        assertThat(byteBuddyType.getTraits(), not(hasItem(TypeTrait.PRIMITIVE)));
    }

    @Test
    public void testJavaLangObj() {

        assertThat(typeResolver.get(Object.class).getTraits(), hasItem(TypeTrait.JAVA_LANG_OBJECT));
    }

    @Test
    public void testJavaLangStr() {

        assertThat(typeResolver.get(String.class).getTraits(), hasItem(TypeTrait.JAVA_LANG_STRING));
    }

    @Test
    public void testMapTraits() {

        assertThat(typeResolver.get(Map.class).getTraits(), hasItem(TypeTrait.MAP));

        assertThat(typeResolver.get(HashMap.class).getTraits(), hasItem(TypeTrait.MAP));

        assertThat(typeResolver.get(LinkedHashMap.class).getTraits(), hasItem(TypeTrait.MAP));
    }

    @Test
    @Ignore
    public void testBaseClassNamesResolve() {
        Type type = typeResolver.get(TestClass.class);

        Assert.assertEquals(
            new HashSet<String>() {{
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
                add("I1");
                add("I2");
                add("I3");
                add("I4");
                add("I5");
            }},
            type.getSuperTypeSimpleNames()
        );
    }

    public enum TestEnum {
        A,
        B,
        C
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

    public abstract class CustomAbstractList implements List<String> {

    }
}