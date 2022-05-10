package com.ulyp.core.recorders;

import com.ulyp.core.Type;

public class ClassObjectRecord extends ObjectRecord {

    private final Type carriedType;

    protected ClassObjectRecord(Type type, Type carriedType) {
        super(type);

        if (!type.getName().equals(Class.class.getName())) {
            throw new RuntimeException("Type must always be a " + Class.class.getName());
        }

        this.carriedType = carriedType;
    }

    public Type getCarriedType() {
        return carriedType;
    }

    @Override
    public String toString() {
        return "class " + carriedType.getName();
    }
}
