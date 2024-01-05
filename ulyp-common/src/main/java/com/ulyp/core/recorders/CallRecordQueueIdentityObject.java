package com.ulyp.core.recorders;

import com.ulyp.core.Type;

import lombok.Getter;

@Getter
public class CallRecordQueueIdentityObject {

    private final Type type;
    private final Class<?> clazz;
    private final int identityHashCode;

    public CallRecordQueueIdentityObject(Type type, Object value) {
        this.type = type;
        this.clazz = value.getClass();
        this.identityHashCode = System.identityHashCode(value);
    }
}
