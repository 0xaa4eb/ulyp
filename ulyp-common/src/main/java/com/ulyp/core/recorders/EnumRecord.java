package com.ulyp.core.recorders;

import com.ulyp.core.Type;

public class EnumRecord extends ObjectRecord {

    private final String name;

    public EnumRecord(Type type, String name) {
        super(type);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
