package com.ulyp.core.printers;

import java.util.List;

public class CollectionRepresentation extends ObjectRepresentation {

    private final int length;
    private final List<ObjectRepresentation> recordedItems;

    // Not all elements are recorded, therefore objectsRepresentations.size() != length
    protected CollectionRepresentation(TypeInfo typeInfo, int length, List<ObjectRepresentation> recordedItems) {
        super(typeInfo);

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
