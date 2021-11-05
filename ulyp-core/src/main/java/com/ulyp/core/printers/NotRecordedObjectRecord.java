package com.ulyp.core.printers;

import com.ulyp.core.Type;

/**
 * This object states that return value has not yet been recorded
 *
 * Recordins are sent through almost fixed size chunks, meaning at some point of time
 * there could be no return value at all (i.e. not recorded yet)
 */
public class NotRecordedObjectRecord extends ObjectRecord {

    private static final ObjectRecord INSTANCE = new NotRecordedObjectRecord(Type.unknown());

    public static ObjectRecord getInstance() {
        return INSTANCE;
    }

    private NotRecordedObjectRecord(Type type) {
        super(type);
    }
}
