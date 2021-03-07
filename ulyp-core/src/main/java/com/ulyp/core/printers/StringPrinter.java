package com.ulyp.core.printers;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.DecodingContext;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

public class StringPrinter extends ObjectBinaryPrinter {

    protected StringPrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(TypeInfo type) {
        return type.isExactlyJavaLangString();
    }

    @Override
    public ObjectRepresentation read(TypeInfo objectType, BinaryInput input, DecodingContext decodingContext) {
        return new StringObjectRepresentation(objectType, input.readString());
    }

    @Override
    public void write(Object object, TypeInfo classDescription, BinaryOutput out, TypeResolver runtime) throws Exception {
        out.writeString((String) object);
    }
}
