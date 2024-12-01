package com.ulyp.core.recorders.basic;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;

public class ThrowableRecord extends ObjectRecord {

    private final ObjectRecord message;

    protected ThrowableRecord(Type type, ObjectRecord message) {
        super(type);

        this.message = message;
    }

    public ObjectRecord getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "exception: " + message;
    }
}
