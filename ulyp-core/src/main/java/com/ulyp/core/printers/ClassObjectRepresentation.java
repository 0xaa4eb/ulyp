package com.ulyp.core.printers;

public class ClassObjectRepresentation extends ObjectRepresentation {

    private final TypeInfo carriedType;

    protected ClassObjectRepresentation(TypeInfo typeInfo, TypeInfo carriedType) {
        super(typeInfo);

        if (!typeInfo.getName().equals(Class.class.getName())) {
            throw new RuntimeException("Type must always be a " + Class.class.getName());
        }

        this.carriedType = carriedType;
    }

    public TypeInfo getCarriedType() {
        return carriedType;
    }
}
