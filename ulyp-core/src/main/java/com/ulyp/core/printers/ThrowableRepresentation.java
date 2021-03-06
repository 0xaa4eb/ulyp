package com.ulyp.core.printers;

public class ThrowableRepresentation extends ObjectRepresentation {

    private final String message;

    protected ThrowableRepresentation(TypeInfo type, String message) {
        super(type);

        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
