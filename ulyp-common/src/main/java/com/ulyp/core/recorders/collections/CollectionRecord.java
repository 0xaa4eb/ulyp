package com.ulyp.core.recorders.collections;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;
import lombok.Getter;

import java.util.List;

@Getter
public class CollectionRecord extends ObjectRecord {

    private final CollectionType collectionType;
    private final int size;
    private final List<ObjectRecord> elements;

    // Not all elements are recorded, therefore objectsRepresentations.size() != length
    protected CollectionRecord(Type type, CollectionType collectionType, int size, List<ObjectRecord> elements) {
        super(type);

        this.collectionType = collectionType;
        this.size = size;
        this.elements = elements;
    }

    @Override
    public String toString() {
        return "type: " + collectionType + " len: " + size + ", elements: " + elements.toString();
    }
}
