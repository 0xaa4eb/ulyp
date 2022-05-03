package com.ulyp.core.recorders;

import com.ulyp.core.Type;

public class ThrowableRecord extends ObjectRecord {

    private final ObjectRecord message;

    protected ThrowableRecord(Type type, ObjectRecord message) {
        super(type);

        this.message = message;
    }

    public ObjectRecord getMessage() {
        return message;
    }
}
