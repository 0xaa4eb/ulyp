package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.Type;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TypeListTest {

    private final TypeList types = new TypeList();

    @Test
    public void testAddAndIterate() {
        types.add(
                Type.builder()
                        .id(534L)
                        .name("com.test.A")
                        .build()
        );

        types.add(
                Type.builder()
                        .id(4241L)
                        .name("com.test.C")
                        .build()
        );

        assertEquals(2, types.size());

        AddressableItemIterator<Type> iterator = types.iterator();
        Type first = iterator.next();
        assertEquals(534L, first.getId());
        assertEquals("com.test.A", first.getName());

        Type second = iterator.next();

        assertEquals(4241L, second.getId());
        assertEquals("com.test.C", second.getName());

        assertFalse(iterator.hasNext());
    }
}