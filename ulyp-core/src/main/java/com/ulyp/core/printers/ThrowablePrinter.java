package com.ulyp.core.printers;

import com.ulyp.core.DecodingContext;
import com.ulyp.core.AgentRuntime;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

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
        return new ThrowableRepresentation(type, input.readString());
    }

    @Override
    public void write(Object object, TypeInfo classDescription, BinaryOutput out, AgentRuntime runtime) throws Exception {
        Throwable t = (Throwable) object;
        out.writeString(t.getMessage());
    }
}
