package com.ulyp.core.recorders.basic;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
public class MethodRecord extends ObjectRecord {

    private final Type declaringType;
    private final String name;

    protected MethodRecord(Type type, String name, Type declaringType) {
        super(type);

        if (!type.getName().equals(Method.class.getName())) {
            throw new RuntimeException("Type must always be a " + Class.class.getName());
        }

        this.name = name;
        this.declaringType = declaringType;
    }

    @Override
    public String toString() {
        return "class " + declaringType.getName();
    }
}
