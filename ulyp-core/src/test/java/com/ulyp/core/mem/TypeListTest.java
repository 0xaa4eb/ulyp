package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.Type;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class TypeListTest {

    private final TypeList types = new TypeList();

    @Test
    public void testAddAndIterate() {
        types.add(
                Type.builder()
                        .id(534L)
                        .name("com.test.A")
                        .superTypeNames(new HashSet<>(Arrays.asList("com.test.B")))
                        .superTypeSimpleNames(new HashSet<>(Arrays.asList("B")))
                        .build()
        );

        types.add(
                Type.builder()
                        .id(4241L)
                        .name("com.test.C")
                        .superTypeNames(new HashSet<>(Arrays.asList("com.test.D")))
                        .superTypeSimpleNames(new HashSet<>(Arrays.asList("D")))
                        .build()
        );

        assertEquals(2, types.size());

        AddressableItemIterator<Type> iterator = types.iterator();
        Type first = iterator.next();
        assertEquals(534L, first.getId());
        assertEquals("com.test.A", first.getName());
        assertEquals(new HashSet<>(Arrays.asList("com.test.B")), first.getSuperTypeNames());

        Type second = iterator.next();

        assertEquals(4241L, second.getId());
        assertEquals("com.test.C", second.getName());
        assertEquals(new HashSet<>(Arrays.asList("com.test.D")), second.getSuperTypeNames());

        assertFalse(iterator.hasNext());
    }
}