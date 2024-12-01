package com.ulyp.core.recorders.arrays;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;
import lombok.Getter;

import java.util.List;

@Getter
public class ArrayRecord extends ObjectRecord {

    private final int length;
    private final List<? extends ObjectRecord> elements;

    // Not all elements are recorded, therefore objectsRepresentations.size() != length
    protected ArrayRecord(Type type, int length, List<? extends ObjectRecord> elements) {
        super(type);

        this.length = length;
        this.elements = elements;
    }

    @Override
    public String toString() {
        return "array len: " + length + ", items: " + elements;
    }
}
