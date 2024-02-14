package com.ulyp.core.recorders;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueuedIdentityObject {

    private int typeId; // object is reused -> fields can not final
    private int identityHashCode;

    public QueuedIdentityObject() {
    }

    public QueuedIdentityObject(int typeId, Object value) {
        this.typeId = typeId;
        this.identityHashCode = System.identityHashCode(value);
    }

    @Override
    public String toString() {
        return "QueuedIdentityObject{" +
                "typeId=" + typeId +
                ", identityHashCode=" + identityHashCode +
                '}';
    }
}
