package com.ulyp.core.printers;

import com.ulyp.core.Type;

// TODO rename
public class StringObjectRecord extends ObjectRecord {

    private final String value;

    public StringObjectRecord(Type classDescription, String value) {
        super(classDescription);
        this.value = value;
    }

    public String value() {
        return value;
    }
}
