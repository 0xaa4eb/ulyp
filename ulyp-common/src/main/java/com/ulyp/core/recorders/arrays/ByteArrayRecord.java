package com.ulyp.core.recorders.arrays;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.IdentityObjectRecord;

public class ByteArrayRecord extends IdentityObjectRecord {

    private final int length;

    protected ByteArrayRecord(Type type, int hashCode, int length) {
        super(type, hashCode);

        this.length = length;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "array len: " + length;
    }
}
