package com.ulyp.core.repository;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InMemoryRepositoryTest {

    private final InMemoryRepository<Long, String> repository = new InMemoryRepository<>();

    @Test
    public void testListener() {
        List<String> values1 = new ArrayList<>();
        repository.subscribe((k, v) -> values1.add(v));

        repository.store(1L, "A");

        Assert.assertEquals(Arrays.asList("A"), values1);

        repository.store(1L, "B");

        Assert.assertEquals(Arrays.asList("A"), values1);

        repository.store(2L, "C");

        Assert.assertEquals(Arrays.asList("A", "C"), values1);

        List<String> values2 = new ArrayList<>();
        repository.subscribe((k, v) -> values2.add(v));

        repository.store(3L, "D");

        Assert.assertEquals(Arrays.asList("A", "C", "D"), values1);
        Assert.assertEquals(Arrays.asList("D"), values2);

    }
}