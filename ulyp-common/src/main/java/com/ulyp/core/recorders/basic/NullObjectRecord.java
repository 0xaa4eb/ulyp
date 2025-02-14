package com.ulyp.core.recorders.basic;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;

public class NullObjectRecord extends ObjectRecord {

    private static final NullObjectRecord instance = new NullObjectRecord();

    private NullObjectRecord() {
        super(Type.unknown());
    }

    public static NullObjectRecord getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "null";
    }
}
