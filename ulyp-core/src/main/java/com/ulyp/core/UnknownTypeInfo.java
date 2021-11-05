package com.ulyp.core;

import com.ulyp.core.printers.ObjectRecorder;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class UnknownTypeInfo implements TypeInfo {

    private static final TypeInfo INSTANCE = new UnknownTypeInfo();

    private static final Set<TypeTrait> EMPTY_TRAITS = EnumSet.noneOf(TypeTrait.class);

    public static TypeInfo getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean wasDumped() {
        return false;
    }

    @Override
    public void markDumped() {

    }

    @Override
    public int getId() {
        return -1;
    }

    @Override
    public String getName() {
        return "UNKNOWN";
    }

    @Override
    public Set<TypeTrait> getTraits() {
        return EMPTY_TRAITS;
    }

    @Override
    public Set<String> getSuperClassesNames() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getInterfacesClassesNames() {
        return Collections.emptySet();
    }

    @Override
    public ObjectRecorder getSuggestedPrinter() {
        return null;
    }

    @Override
    public boolean isExactlyJavaLangObject() {
        return false;
    }

    @Override
    public boolean isExactlyJavaLangString() {
        return false;
    }

    @Override
    public boolean isNonPrimitveArray() {
        return false;
    }

    @Override
    public boolean isPrimitiveArray() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public boolean isTypeVar() {
        return false;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public boolean isClassObject() {
        return false;
    }

    @Override
    public boolean hasToStringMethod() {
        return false;
    }
}
