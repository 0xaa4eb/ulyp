package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class ToStringPrintedRepresentation extends ObjectRepresentation {

    private final ObjectRepresentation printed;
    private final int identityHashCode;

    protected ToStringPrintedRepresentation(ObjectRepresentation printed, Type type, int identityHashCode) {
        super(type);

        this.printed = printed;
        this.identityHashCode = identityHashCode;
    }

    public ObjectRepresentation getPrinted() {
        return printed;
    }

    public int getIdentityHashCode() {
        return identityHashCode;
    }
}
