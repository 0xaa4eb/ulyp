package com.ulyp.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

class MethodRepositoryTest {

    private MethodRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MethodRepository();
    }

    @Test
    void shouldStoreAndRetrieveMethod() {
        Method method = Method.builder()
            .name("testMethod")
            .type(Type.builder()
                .name("TestClass")
                .build())
            .build();
        
        int id = repository.putAndGetId(method);
        
        Method retrieved = repository.get(id);
        assertEquals(method, retrieved, "Retrieved method should match the stored method");
        assertEquals(id, retrieved.getId(), "Method ID should be set correctly");
    }

    @Test
    void shouldHandleMultipleMethods() {
        Method method1 = Method.builder()
            .name("testMethod1")
            .type(Type.builder()
                .name("TestClass1")
                .build())
            .build();
        Method method2 = Method.builder()
            .name("testMethod2")
            .type(Type.builder()
                .name("TestClass2")
                .build())
            .build();
        
        int id1 = repository.putAndGetId(method1);
        int id2 = repository.putAndGetId(method2);
        
        assertNotEquals(id1, id2, "Different methods should have different IDs");
        assertEquals(method1, repository.get(id1), "First method should be retrieved correctly");
        assertEquals(method2, repository.get(id2), "Second method should be retrieved correctly");
    }

    @Test
    void shouldReturnNullForNonExistentId() {
        assertNull(repository.get(999), "Should return null for non-existent method ID");
    }

    @Test
    void shouldReturnAllStoredMethods() {
        Method method1 = Method.builder()
            .name("testMethod1")
            .type(Type.builder()
                .name("TestClass1")
                .build())
            .build();
        Method method2 = Method.builder()
            .name("testMethod2")
            .type(Type.builder()
                .name("TestClass2")
                .build())
            .build();
        Method method3 = Method.builder()
            .name("testMethod3")
            .type(Type.builder()
                .name("TestClass3")
                .build())
            .build();
        
        repository.putAndGetId(method1);
        repository.putAndGetId(method2);
        repository.putAndGetId(method3);
        
        Collection<Method> values = repository.values();
        
        assertEquals(3, values.size(), "Should return all stored methods");
        assertTrue(values.contains(method1), "Should contain first method");
        assertTrue(values.contains(method2), "Should contain second method");
        assertTrue(values.contains(method3), "Should contain third method");
    }

    @Test
    void shouldHandleEmptyRepository() {
        Collection<Method> values = repository.values();
        
        assertTrue(values.isEmpty(), "Values should be empty for new repository");
    }

    @Test
    void shouldPreserveMethodOrder() {
        Method method1 = Method.builder()
            .name("testMethod1")
            .type(Type.builder()
                .name("TestClass1")
                .build())
            .build();
        Method method2 = Method.builder()
            .name("testMethod2")
            .type(Type.builder()
                .name("TestClass2")
                .build())
            .build();
        
        int id1 = repository.putAndGetId(method1);
        int id2 = repository.putAndGetId(method2);
        
        assertEquals(0, id1, "First method should get ID 0");
        assertEquals(1, id2, "Second method should get ID 1");
    }
} 