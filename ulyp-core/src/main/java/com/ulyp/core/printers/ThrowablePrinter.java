package com.ulyp.core.printers;

import com.ulyp.core.DecodingContext;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;
import com.ulyp.core.printers.bytes.BinaryOutputAppender;

public class ThrowablePrinter extends ObjectBinaryPrinter {

    protected ThrowablePrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(TypeInfo type) {
        return type.getTraits().contains(TypeTrait.THROWABLE);
    }

    @Override
    public ObjectRepresentation read(TypeInfo type, BinaryInput input, DecodingContext decodingContext) {
        return new ThrowableRepresentation(type, input.readObject(decodingContext));
    }

    @Override
    public void write(Object object, TypeInfo classDescription, BinaryOutput out, TypeResolver runtime) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {
            Throwable t = (Throwable) object;
            appender.append(t.getMessage(), runtime);
        }
    }
}
