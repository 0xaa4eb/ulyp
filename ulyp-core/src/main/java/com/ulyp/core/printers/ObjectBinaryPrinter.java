package com.ulyp.core.printers;

import com.ulyp.core.DecodingContext;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

public abstract class ObjectBinaryPrinter {

    private final byte id;

    protected ObjectBinaryPrinter(byte id) {
        this.id = id;
    }

    public final byte getId() {
        return id;
    }

    public abstract ObjectRepresentation read(TypeInfo objectType, BinaryInput input, DecodingContext decodingContext);

    abstract boolean supports(TypeInfo type);

    /**
     * @param object object to print
     * @param objectType a type of the object to print
     * @param out target binary stream to print to
     * @param runtime runtime provided by instrumentation library
     */
    public abstract void write(Object object, TypeInfo objectType, BinaryOutput out, TypeResolver runtime) throws Exception;

    /**
     * @param obj object to print
     * @param out target binary stream to print to
     * @param typeResolver runtime provided by instrumentation library
     */
    // TODO retire this
    public void write(Object obj, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        write(obj, typeResolver.get(obj), out, typeResolver);
    }
}
