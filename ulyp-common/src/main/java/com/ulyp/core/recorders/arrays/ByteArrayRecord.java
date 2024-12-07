package com.ulyp.core.recorders.arrays;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import lombok.Getter;

public class ByteArrayRecord extends ObjectRecord {

    @Getter
    private final IdentityObjectRecord identityRecord;
    @Getter
    private final int length;

    protected ByteArrayRecord(Type type, IdentityObjectRecord identityRecord, int length) {
        super(type);

        this.length = length;
        this.identityRecord = identityRecord;
    }

    @Override
    public String toString() {
        return "array len: " + length;
    }
}
