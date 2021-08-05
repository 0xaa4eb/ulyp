package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class EnumRepresentation extends ObjectRepresentation {

    private final String name;

    public EnumRepresentation(Type type, String name) {
        super(type);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
