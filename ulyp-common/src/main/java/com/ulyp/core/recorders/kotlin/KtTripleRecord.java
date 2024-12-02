package com.ulyp.core.recorders.kotlin;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class KtTripleRecord extends ObjectRecord {

    private final ObjectRecord first;
    private final ObjectRecord second;
    private final ObjectRecord third;

    protected KtTripleRecord(@NotNull Type type, ObjectRecord first, ObjectRecord second, ObjectRecord third) {
        super(type);
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public String toString() {
        return "<" + first + ", " + second + ", " + third + ">";
    }
}
