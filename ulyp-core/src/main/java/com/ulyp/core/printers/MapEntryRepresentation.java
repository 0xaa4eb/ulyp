package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class MapEntryRepresentation extends ObjectRepresentation {

    private final ObjectRepresentation key;
    private final ObjectRepresentation value;

    protected MapEntryRepresentation(Type type, ObjectRepresentation key, ObjectRepresentation value) {
        super(type);

        this.key = key;
        this.value = value;
    }

    public ObjectRepresentation getKey() {
        return key;
    }

    public ObjectRepresentation getValue() {
        return value;
    }
}
