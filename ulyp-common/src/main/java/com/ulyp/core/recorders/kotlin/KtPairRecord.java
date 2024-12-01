package com.ulyp.core.recorders.kotlin;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class KtPairRecord extends ObjectRecord {

    private final ObjectRecord first;
    private final ObjectRecord second;

    protected KtPairRecord(@NotNull Type type, ObjectRecord first, ObjectRecord second) {
        super(type);
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "<" + first + ", " + second + ">";
    }
}
