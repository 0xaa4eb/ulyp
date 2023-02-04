package com.ulyp.core.recorders.arrays;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;

import java.util.List;

public class ObjectArrayRecord extends ObjectRecord {

    private final int length;
    private final List<ObjectRecord> recordedItems;

    // Not all elements are recorded, therefore objectsRepresentations.size() != length
    protected ObjectArrayRecord(Type type, int length, List<ObjectRecord> recordedItems) {
        super(type);

        this.length = length;
        this.recordedItems = recordedItems;
    }

    public int getLength() {
        return length;
    }

    public List<ObjectRecord> getRecordedItems() {
        return recordedItems;
    }

    @Override
    public String toString() {
        return "array len: " + length + ", items: " + recordedItems;
    }
}
