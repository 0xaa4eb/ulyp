package com.ulyp.core.recorders;

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

    @Override
    public String toString() {
        return getType().getName() + "@" + hashCode;
    }
}
