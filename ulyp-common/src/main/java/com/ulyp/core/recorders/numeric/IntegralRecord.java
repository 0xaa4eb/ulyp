package com.ulyp.core.recorders.numeric;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;
import lombok.Getter;

@Getter
public class IntegralRecord extends ObjectRecord {

    private final long value;

    public IntegralRecord(Type type, long value) {
        super(type);
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
