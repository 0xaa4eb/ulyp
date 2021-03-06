package com.ulyp.core;

import com.ulyp.core.printers.ObjectBinaryPrinter;
import com.ulyp.core.printers.TypeInfo;
import com.ulyp.core.printers.TypeTrait;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class NameOnlyTypeInfo implements TypeInfo {

    private final int id;
    private final String name;

    public NameOnlyTypeInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<TypeTrait> getTraits() {
        return EnumSet.noneOf(TypeTrait.class);
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
    public ObjectBinaryPrinter getSuggestedPrinter() {
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
