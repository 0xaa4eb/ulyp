package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class IdentityObjectRecord extends ObjectRecord {

    private final int hashCode;

    public IdentityObjectRecord(Type type, int hashCode) {
        super(type);
        this.hashCode = hashCode;
    }

    public int getHashCode() {
        return hashCode;
    }
}
