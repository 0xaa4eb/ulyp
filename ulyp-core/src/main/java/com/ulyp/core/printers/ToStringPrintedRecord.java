package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class ToStringPrintedRecord extends ObjectRecord {

    private final ObjectRecord printed;
    private final int identityHashCode;

    protected ToStringPrintedRecord(ObjectRecord printed, Type type, int identityHashCode) {
        super(type);

        this.printed = printed;
        this.identityHashCode = identityHashCode;
    }

    public ObjectRecord getPrinted() {
        return printed;
    }

    public int getIdentityHashCode() {
        return identityHashCode;
    }
}
