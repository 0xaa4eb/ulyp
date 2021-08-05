package com.ulyp.core.printers;

import com.ulyp.core.Type;

import java.util.List;

public class ObjectArrayRepresentation extends ObjectRepresentation {

    private final int length;
    private final List<ObjectRepresentation> recordedItems;

    // Not all elements are recorded, therefore objectsRepresentations.size() != length
    protected ObjectArrayRepresentation(Type type, int length, List<ObjectRepresentation> recordedItems) {
        super(type);

        this.length = length;
        this.recordedItems = recordedItems;
    }

    public int getLength() {
        return length;
    }

    public List<ObjectRepresentation> getRecordedItems() {
        return recordedItems;
    }
}
