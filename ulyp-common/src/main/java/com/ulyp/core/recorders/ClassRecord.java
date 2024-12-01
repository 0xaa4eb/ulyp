package com.ulyp.core.recorders;

import com.ulyp.core.Type;
import lombok.Getter;

@Getter
public class ClassRecord extends ObjectRecord {

    private final Type declaringType;

    protected ClassRecord(Type type, Type declaringType) {
        super(type);

        if (!type.getName().equals(Class.class.getName())) {
            throw new RuntimeException("Type must always be a " + Class.class.getName());
        }

        this.declaringType = declaringType;
    }

    @Override
    public String toString() {
        return "class " + declaringType.getName();
    }
}
