package com.ulyp.core.recorders;

import com.ulyp.core.Type;
import lombok.Getter;

/**
 * Object record which contains only type id and identity hash code (result of calling {@link System#identityHashCode(Object)})
 */
@Getter
public class IdentityObjectRecord extends ObjectRecord {

    private final int hashCode;

    public IdentityObjectRecord(Type type, int hashCode) {
        super(type);
        this.hashCode = hashCode;
    }

    @Override
    public String toString() {
        return getType().getName() + "@" + Integer.toHexString(hashCode);
    }
}
