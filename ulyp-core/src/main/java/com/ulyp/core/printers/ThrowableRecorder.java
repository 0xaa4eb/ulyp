package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.TypeTrait;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;
import com.ulyp.core.printers.bytes.BinaryOutputAppender;

public class ThrowableRecorder extends ObjectRecorder {

    protected ThrowableRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.getTraits().contains(TypeTrait.THROWABLE);
    }

    @Override
    public ObjectRepresentation read(Type type, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new ThrowableRepresentation(type, input.readObject(typeResolver));
    }

    @Override
    public void write(Object object, Type classDescription, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {
            Throwable t = (Throwable) object;
            appender.append(t.getMessage(), typeResolver);
        }
    }
}
