package com.ulyp.core.recorders;

import com.ulyp.core.Type;

import lombok.Getter;

@Getter
public class QueuedIdentityObject {

    private final Type type;
    private final int identityHashCode;

    public QueuedIdentityObject(Type type, Object value) {
        this.type = type;
        this.identityHashCode = System.identityHashCode(value);
    }
}
