package com.ulyp.core.recorders;

import com.ulyp.core.Type;

public class PrintedObjectRecord extends ObjectRecord {

    private final StringObjectRecord printed;
    private final int identityHashCode;

    protected PrintedObjectRecord(StringObjectRecord printed, Type type, int identityHashCode) {
        super(type);

        this.printed = printed;
        this.identityHashCode = identityHashCode;
    }

    public StringObjectRecord getPrinted() {
        return printed;
    }

    public int getIdentityHashCode() {
        return identityHashCode;
    }

    @Override
    public String toString() {
        return printed.toString();
    }
}
