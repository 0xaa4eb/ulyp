package com.ulyp.core.printers;

import com.ulyp.core.Type;

import java.util.List;

public class MapRepresentation extends ObjectRepresentation {

    private final int size;
    private final List<MapEntryRepresentation> entries;

    // Not all elements are recorded, therefore objectsRepresentations.size() != length
    protected MapRepresentation(Type type, int size, List<MapEntryRepresentation> entries) {
        super(type);

        this.size = size;
        this.entries = entries;
    }

    public int getSize() {
        return size;
    }

    public List<MapEntryRepresentation> getEntries() {
        return entries;
    }
}
