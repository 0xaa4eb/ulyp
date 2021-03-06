package com.ulyp.core.printers;

/**
 * This object states that return value has not yet been recorded
 *
 * Recordins are sent through almost fixed size chunks, meaning at some point of time
 * there could be no return value at all (i.e. not recorded yet)
 */
public class NotRecordedObjectRepresentation extends ObjectRepresentation {

    private static final ObjectRepresentation INSTANCE = new NotRecordedObjectRepresentation(UnknownTypeInfo.getInstance());

    public static ObjectRepresentation getInstance() {
        return INSTANCE;
    }

    private NotRecordedObjectRepresentation(TypeInfo typeInfo) {
        super(typeInfo);
    }
}
