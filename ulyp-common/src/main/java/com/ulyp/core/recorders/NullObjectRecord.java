package com.ulyp.core.recorders;

import com.ulyp.core.Type;

public class NullObjectRecord extends ObjectRecord {

    private static final ObjectRecord instance = new NullObjectRecord();

    private NullObjectRecord() {
        super(Type.unknown());
    }

    public static ObjectRecord getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "null";
    }
}
