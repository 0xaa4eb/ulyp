package com.ulyp.core.printers;

import com.ulyp.core.DecodingContext;
import com.ulyp.core.AgentRuntime;
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
        ObjectBinaryPrinter printer = ObjectBinaryPrinterType.printerForId(input.readByte());
        TypeInfo msgType = decodingContext.getType(input.readInt());
        ObjectRepresentation message = printer.read(msgType, input, decodingContext);
        return new ThrowableRepresentation(type, message);
    }

    @Override
    public void write(Object object, TypeInfo classDescription, BinaryOutput out, AgentRuntime runtime) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {
            Throwable t = (Throwable) object;
            String message = t.getMessage();
            ObjectBinaryPrinter printer = message != null ? ObjectBinaryPrinterType.STRING_PRINTER.getInstance() : ObjectBinaryPrinterType.NULL_PRINTER.getInstance();
            appender.append(printer.getId());
            TypeInfo msgType = runtime.get(message);
            appender.append(msgType.getId());
            printer.write(message, msgType, appender, runtime);
        }
    }
}
