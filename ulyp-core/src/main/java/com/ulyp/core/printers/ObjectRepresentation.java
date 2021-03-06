package com.ulyp.core.printers;

/**
 * Deserialized object representation. Depending on the printer used for serialization
 * some amount of information may (or may not) be lost
 */
public abstract class ObjectRepresentation {

    private final TypeInfo typeInfo;

    protected ObjectRepresentation(TypeInfo typeInfo) {
        this.typeInfo = typeInfo;
    }

    public TypeInfo getType() {
        return typeInfo;
    }
}
