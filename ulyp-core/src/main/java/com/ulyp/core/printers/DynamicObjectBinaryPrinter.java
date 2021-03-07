package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;
import com.ulyp.core.printers.bytes.BinaryOutputAppender;

public class DynamicObjectBinaryPrinter extends ObjectBinaryPrinter {

    protected DynamicObjectBinaryPrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(TypeInfo type) {
        return type.isInterface() || type.isExactlyJavaLangObject() || type.isTypeVar();
    }

    @Override
    public ObjectRepresentation read(TypeInfo objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        byte printerId = input.readByte();
        return ObjectBinaryPrinterType.printerForId(printerId).read(objectType, input, typeResolver);
    }

    @Override
    public void write(Object object, TypeInfo objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        ObjectBinaryPrinter printer = objectType.getSuggestedPrinter();
        if (printer.getId() == getId()) {
            printer = ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance();
        }

        try (BinaryOutputAppender appender = out.appender()) {
            appender.append(printer.getId());
            printer.write(object, appender, typeResolver);
        }
    }
}
