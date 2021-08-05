package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class IdentityObjectRepresentation extends ObjectRepresentation {

    private final int hashCode;

    public IdentityObjectRepresentation(Type type, int hashCode) {
        super(type);
        this.hashCode = hashCode;
    }

    public int getHashCode() {
        return hashCode;
    }
}
