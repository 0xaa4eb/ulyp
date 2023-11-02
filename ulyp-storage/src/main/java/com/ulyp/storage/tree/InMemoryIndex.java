package com.ulyp.storage.tree;

import com.ulyp.core.repository.InMemoryRepository;
import com.ulyp.core.repository.Repository;

public class InMemoryIndex implements Index {

    private final Repository<Long, RecordedCallState> repository = new InMemoryRepository<>();

    @Override
    public RecordedCallState get(long id) {
        return repository.get(id);
    }

    @Override
    public void store(long id, RecordedCallState callState) {
        repository.store(id, callState);
    }

    @Override
    public void close() throws Exception {
        // NOP
    }
}
