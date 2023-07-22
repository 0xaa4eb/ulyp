package com.ulyp.core.recorders;

import com.ulyp.core.Type;

public class CharRecord extends ObjectRecord {

    private final char value;

    public CharRecord(Type type, char value) {
        super(type);
        this.value = value;
    }

    public char getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

