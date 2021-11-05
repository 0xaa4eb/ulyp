package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class NullObjectRecord extends ObjectRecord {

    private static final ObjectRecord instance = new NullObjectRecord();

    public static ObjectRecord getInstance() {
        return instance;
    }

    private NullObjectRecord() {
        super(Type.unknown());
    }
}
