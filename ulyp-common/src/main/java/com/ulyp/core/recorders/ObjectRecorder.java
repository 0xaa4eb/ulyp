package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
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

    abstract boolean supports(Type type);

    public abstract ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver);

    public abstract void write(Object object, @NotNull Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception;

    public void write(Object obj, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        write(obj, typeResolver.get(obj), out, typeResolver);
    }

    @Override
    public String toString() {
        return simpleClassName + "{}";
    }
}
