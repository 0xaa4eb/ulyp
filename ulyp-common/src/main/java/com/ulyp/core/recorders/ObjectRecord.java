package com.ulyp.core.recorders;

import com.ulyp.core.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

/**
 * Recorded object value. Depending on the recorder chosen for recording
 * some amount of information might (and for some data types certainly will) be lost
 * <p>
 * For example, for strings the corresponding recorder {@link StringRecorder} will only record first 200 symbols, for complex objects
 * only type and identity hash code are recorded, see {@link IdentityRecorder}.
 */
public abstract class ObjectRecord {

    @NotNull
    private final Type type;

    protected ObjectRecord(@NotNull Type type) {
        this.type = type;
    }

    @NotNull
    public Type getType() {
        return type;
    }

    // Only used in unit tests
    @TestOnly
    public abstract String toString();
}
