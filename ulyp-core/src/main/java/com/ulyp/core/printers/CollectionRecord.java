package com.ulyp.core.printers;

import com.ulyp.core.Type;

import java.util.List;

public class CollectionRecord extends ObjectRecord {

    private final int length;
    private final List<ObjectRecord> recordedItems;

    // Not all elements are recorded, therefore objectsRepresentations.size() != length
    protected CollectionRecord(Type type, int length, List<ObjectRecord> recordedItems) {
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
}
