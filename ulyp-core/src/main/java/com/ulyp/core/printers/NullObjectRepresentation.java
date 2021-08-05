package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class NullObjectRepresentation extends ObjectRepresentation {

    private static final ObjectRepresentation instance = new NullObjectRepresentation();

    public static ObjectRepresentation getInstance() {
        return instance;
    }

    private NullObjectRepresentation() {
        super(Type.unknown());
    }
}
