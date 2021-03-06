package com.ulyp.core.printers;

public class EnumRepresentation extends ObjectRepresentation {

    private final String name;

    public EnumRepresentation(TypeInfo typeInfo, String name) {
        super(typeInfo);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
