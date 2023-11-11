package com.ulyp.storage.tree;

import java.util.function.Supplier;

import com.ulyp.storage.reader.RecordingDataReader;

public class CallRecordTreeBuilder {

    private final RecordingDataReader dataReader;
    private boolean readInfinitely = true;
    private RecordingListener recordingListener = RecordingListener.empty();
    private Supplier<Index> indexSupplier = InMemoryIndex::new;

    public CallRecordTreeBuilder(RecordingDataReader dataReader) {
        this.dataReader = dataReader;
    }

    public CallRecordTreeBuilder setReadInfinitely(boolean readInfinitely) {
        this.readInfinitely = readInfinitely;
        return this;
    }

    public CallRecordTreeBuilder setRecordingListener(RecordingListener recordingListener) {
        this.recordingListener = recordingListener;
        return this;
    }

    public CallRecordTreeBuilder setIndexSupplier(Supplier<Index> indexSupplier) {
        this.indexSupplier = indexSupplier;
        return this;
    }

    public CallRecordTree build() {
        return new CallRecordTree(dataReader, recordingListener, indexSupplier, readInfinitely);
    }
}
