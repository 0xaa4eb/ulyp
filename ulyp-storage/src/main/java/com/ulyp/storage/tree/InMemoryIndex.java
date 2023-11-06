package com.ulyp.storage.tree;

import com.ulyp.core.repository.InMemoryRepository;
import com.ulyp.core.repository.Repository;

public class InMemoryIndex implements Index {

    private final Repository<Long, CallRecordIndexState> repository = new InMemoryRepository<>();

    @Override
    public CallRecordIndexState get(long id) {
        return repository.get(id);
    }

    @Override
    public void store(long id, CallRecordIndexState callState) {
        repository.store(id, callState);
    }

    @Override
    public void close() throws RuntimeException {
        // NOP
    }
}
