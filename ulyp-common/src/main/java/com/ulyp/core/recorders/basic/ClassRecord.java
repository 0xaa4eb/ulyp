package com.ulyp.core.recorders.basic;

import com.ulyp.core.Type;
import com.ulyp.core.exception.UlypException;
import com.ulyp.core.recorders.ObjectRecord;
import lombok.Getter;

@Getter
public class ClassRecord extends ObjectRecord {

    private final Type declaringType;

    protected ClassRecord(Type type, Type declaringType) {
        super(type);

        if (!type.getName().equals(Class.class.getName())) {
            throw new UlypException("Type must always be a " + Class.class.getName());
        }

        this.declaringType = declaringType;
    }

    @Override
    public String toString() {
        return declaringType.getName();
    }
}
