package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
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
    public ObjectRepresentation read(TypeInfo type, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new ThrowableRepresentation(type, input.readObject(typeResolver));
    }

    @Override
    public void write(Object object, TypeInfo classDescription, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {
            Throwable t = (Throwable) object;
            appender.append(t.getMessage(), typeResolver);
        }
    }
}
