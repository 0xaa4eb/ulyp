package com.ulyp.core.printers;

public class ToStringPrintedRepresentation extends ObjectRepresentation {

    private final ObjectRepresentation printed;
    private final int identityHashCode;

    protected ToStringPrintedRepresentation(ObjectRepresentation printed, TypeInfo typeInfo, int identityHashCode) {
        super(typeInfo);

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
