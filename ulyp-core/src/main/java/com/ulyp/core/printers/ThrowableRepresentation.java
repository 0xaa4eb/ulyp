package com.ulyp.core.printers;

public class ThrowableRepresentation extends ObjectRepresentation {

    private final ObjectRepresentation message;

    protected ThrowableRepresentation(TypeInfo type, ObjectRepresentation message) {
        super(type);

        this.message = message;
    }

    public ObjectRepresentation getMessage() {
        return message;
    }
}
