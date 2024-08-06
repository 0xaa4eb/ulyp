package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
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

    /**
     * @return true if recording can be done in the background thread. This is usually the case if the supported type
     * is immutable, or the recorder does not access object fields (i.e. only identity hash code and type id is recorded).
     * Returns false otherwise
     */
    public boolean supportsAsyncRecording() {
        return false;
    }

    public abstract ObjectRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver);

    public abstract void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception;

    @Override
    public String toString() {
        return simpleClassName + "{}";
    }
}
