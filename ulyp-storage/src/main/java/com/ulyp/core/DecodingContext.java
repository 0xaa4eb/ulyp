package com.ulyp.core;

public class DecodingContext implements ByIdTypeResolver {

    private final TypeInfoDatabase typeInfoDatabase;

    public DecodingContext(TypeInfoDatabase typeInfoDatabase) {
        this.typeInfoDatabase = typeInfoDatabase;
    }

    public Type getType(long id) {
        return typeInfoDatabase.find(id);
    }
}
