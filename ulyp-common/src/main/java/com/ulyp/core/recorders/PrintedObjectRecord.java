package com.ulyp.core.recorders;

import com.ulyp.core.Type;
import lombok.Getter;

@Getter
public class PrintedObjectRecord extends ObjectRecord {

    private final String printedObject;
    private final int identityHashCode;

    protected PrintedObjectRecord(String printedObject, Type type, int identityHashCode) {
        super(type);

        this.printedObject = printedObject;
        this.identityHashCode = identityHashCode;
    }

    @Override
    public String toString() {
        return printedObject;
    }
}
