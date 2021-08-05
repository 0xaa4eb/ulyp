package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class ThrowableRepresentation extends ObjectRepresentation {

    private final ObjectRepresentation message;

    protected ThrowableRepresentation(Type type, ObjectRepresentation message) {
        super(type);

        this.message = message;
    }

    public ObjectRepresentation getMessage() {
        return message;
    }
}
