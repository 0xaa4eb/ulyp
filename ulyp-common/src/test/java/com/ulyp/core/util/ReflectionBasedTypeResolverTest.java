package com.ulyp.core.util;

import com.ulyp.core.Type;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionBasedTypeResolverTest {

    private final ReflectionBasedTypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldReturnSameTypeForSameClass() {
        Type stringType1 = typeResolver.get(String.class);
        Type stringType2 = typeResolver.get(String.class);

        assertEquals(stringType1, stringType2, 
            "Same class should resolve to the same Type instance");
        assertEquals(stringType1.getName(), String.class.getName(), 
            "Type name should match class name");
    }

    @Test
    void shouldReturnSameTypeForSameObject() {
        String testString = "test";
        Type type1 = typeResolver.get(testString);
        Type type2 = typeResolver.get(testString);

        assertEquals(type1, type2, 
            "Same object should resolve to the same Type instance");
        assertEquals(type1.getName(), String.class.getName(), 
            "Type name should match object's class name");
    }

    @Test
    void shouldReturnUnknownTypeForNull() {
        Type type = typeResolver.get((Object) null);

        assertEquals(Type.unknown(), type, 
            "Null object should resolve to unknown type");
    }

    @Test
    void shouldGenerateUniqueIdsForDifferentTypes() {
        Type stringType = typeResolver.get(String.class);
        Type integerType = typeResolver.get(Integer.class);

        assertNotEquals(stringType.getId(), integerType.getId(), 
            "Different types should have different IDs");
    }

    @Test
    void shouldStoreAllResolvedTypes() {
        Type stringType = typeResolver.get(String.class);
        Type integerType = typeResolver.get(Integer.class);
        Type booleanType = typeResolver.get(Boolean.class);

        assertTrue(typeResolver.values().contains(stringType), 
            "Values should contain String type");
        assertTrue(typeResolver.values().contains(integerType), 
            "Values should contain Integer type");
        assertTrue(typeResolver.values().contains(booleanType), 
            "Values should contain Boolean type");
        assertEquals(3, typeResolver.values().size(), 
            "Values should contain exactly three types");
    }
} 