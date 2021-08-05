package com.ulyp.core.printers;

import com.ulyp.core.Type;

// TODO rename
public class StringObjectRepresentation extends ObjectRepresentation {

    private final String value;

    public StringObjectRepresentation(Type classDescription, String value) {
        super(classDescription);
        this.value = value;
    }

    public String value() {
        return value;
    }
}
