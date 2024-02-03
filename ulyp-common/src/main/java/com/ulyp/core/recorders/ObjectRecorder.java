package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BinaryInput;
import com.ulyp.core.bytes.BinaryOutput;
import org.jetbrains.annotations.NotNull;

/**
 * Object recorder which does what it is named for. It essentially encodes some java object of
 * certain type at runtime into bytes which can later be read and decoded.
 * The decoded value is some implementation of {@link ObjectRecord}.
 * <p>
 * Depending on the particular implementation used for serialization
 * some amount of information may (and for some data types certainly will) be lost.
 */
public abstract class ObjectRecorder {

    protected final String simpleClassName;
    private final byte id;

    protected ObjectRecorder(byte id) {
        this.id = id;
        this.simpleClassName = getClass().getSimpleName();
    }

    public final byte getId() {
        return id;
    }

    public abstract boolean supports(Class<?> type);

    public boolean supportsAsyncRecording() {
        return false;
    }

    public abstract ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver);

    public abstract void write(Object object, BinaryOutput out, TypeResolver typeResolver) throws Exception;

    @Override
    public String toString() {
        return simpleClassName + "{}";
    }
}
