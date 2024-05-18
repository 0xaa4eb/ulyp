package com.ulyp.storage.tree;

public class InMemoryIndexTest extends IndexTest {
    @Override
    protected Index buildIndex() {
        return new InMemoryIndex();
    }
}
