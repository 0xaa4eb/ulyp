package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class ClassObjectRepresentation extends ObjectRepresentation {

    private final Type carriedType;

    protected ClassObjectRepresentation(Type type, Type carriedType) {
        super(type);

        if (!type.getName().equals(Class.class.getName())) {
            throw new RuntimeException("Type must always be a " + Class.class.getName());
        }

        this.carriedType = carriedType;
    }

    public Type getCarriedType() {
        return carriedType;
    }
}
