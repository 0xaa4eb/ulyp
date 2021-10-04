package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

/**
 * Object printer which essentially encodes some java object at recording time into bytes which
 * can later be read and decoded. The decoded value is some implementation of {@link ObjectRepresentation}.
 *
 * Depending on the particular implementation used for serialization
 * some amount of information may (and for some data types certainly will) be lost.
 */
public abstract class ObjectBinaryPrinter {

    private final byte id;

    protected ObjectBinaryPrinter(byte id) {
        this.id = id;
    }

    public final byte getId() {
        return id;
    }

    /**
     * @return if this
     */
    abstract boolean supports(Type type);

    public abstract ObjectRepresentation read(Type objectType, BinaryInput input, ByIdTypeResolver typeResolver);

    public abstract void write(Object object, Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception;

    public void write(Object obj, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        write(obj, typeResolver.get(obj), out, typeResolver);
    }
}
