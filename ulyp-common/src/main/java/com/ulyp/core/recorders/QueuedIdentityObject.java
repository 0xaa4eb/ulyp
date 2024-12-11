package com.ulyp.core.recorders;

import lombok.Getter;
import lombok.Setter;

/**
 * Queued identity. Using this class we can effectively block using any recorders on a specific objects. For example,
 * if object is currently undergoes construction (i.e. constructor was called and have not yet returned), then
 * we can't record this object state. Instead, we replace the object by a created instance of this class which
 * only contains identity hash code and type id. It will be recorded by {@link QueuedIdentityObjectRecorder}.
 */
@Getter
@Setter
public class QueuedIdentityObject {

    private final int typeId;
    private final int identityHashCode;

    public QueuedIdentityObject(int typeId, int identityHashCode) {
        this.typeId = typeId;
        this.identityHashCode = identityHashCode;
    }

    @Override
    public String toString() {
        return "QueuedIdentityObject{" +
                "typeId=" + typeId +
                ", identityHashCode=" + identityHashCode +
                '}';
    }
}
