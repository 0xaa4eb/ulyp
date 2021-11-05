package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class MapEntryRecord extends ObjectRecord {

    private final ObjectRecord key;
    private final ObjectRecord value;

    protected MapEntryRecord(Type type, ObjectRecord key, ObjectRecord value) {
        super(type);

        this.key = key;
        this.value = value;
    }

    public ObjectRecord getKey() {
        return key;
    }

    public ObjectRecord getValue() {
        return value;
    }
}
