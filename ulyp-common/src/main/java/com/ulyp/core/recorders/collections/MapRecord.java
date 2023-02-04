package com.ulyp.core.recorders.collections;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;

import java.util.List;

public class MapRecord extends ObjectRecord {

    private final int size;
    private final List<MapEntryRecord> entries;

    // Not all elements are recorded, therefore objectsRepresentations.size() != length
    protected MapRecord(Type type, int size, List<MapEntryRecord> entries) {
        super(type);

        this.size = size;
        this.entries = entries;
    }

    public int getSize() {
        return size;
    }

    public List<MapEntryRecord> getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        return "size: " + size + ", entries: " + entries;
    }
}
