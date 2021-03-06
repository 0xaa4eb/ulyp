package com.ulyp.core.printers;

// TODO rename
public class StringObjectRepresentation extends ObjectRepresentation {

    private final String value;

    public StringObjectRepresentation(TypeInfo classDescription, String value) {
        super(classDescription);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
