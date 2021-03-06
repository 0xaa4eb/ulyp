package com.ulyp.core.printers;

public class NullObjectRepresentation extends ObjectRepresentation {

    private static final ObjectRepresentation instance = new NullObjectRepresentation();

    public static ObjectRepresentation getInstance() {
        return instance;
    }

    private NullObjectRepresentation() {
        super(UnknownTypeInfo.getInstance());
    }
}
