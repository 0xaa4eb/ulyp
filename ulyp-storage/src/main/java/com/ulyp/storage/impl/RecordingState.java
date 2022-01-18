package com.ulyp.storage.impl;

import com.ulyp.core.impl.InMemoryIndex;
import com.ulyp.core.impl.Index;
import com.ulyp.core.mem.RecordedMethodCallList;

import java.io.IOException;

class RecordingState {

    private final ByAddressFileReader reader;
    private final Index index;

    RecordingState(ByAddressFileReader input) {
        this.index = new InMemoryIndex();
        this.reader = input;
    }

    void onRecordedCalls(long addr, RecordedMethodCallList calls) {

    }
}
