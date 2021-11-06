package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.BinaryOutputAppender;

public class DynamicObjectRecorder extends ObjectRecorder {

    protected DynamicObjectRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.isInterface() || type.isExactlyJavaLangObject() || type.isTypeVar();
    }

    @Override
    public ObjectRecord read(Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        byte printerId = input.readByte();
        return ObjectBinaryPrinterType.printerForId(printerId).read(objectType, input, typeResolver);
    }

    @Override
    public void write(Object object, Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        ObjectRecorder printer = objectType.getSuggestedPrinter();
        if (printer.getId() == getId()) {
            printer = ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance();
        }

        try (BinaryOutputAppender appender = out.appender()) {
            appender.append(printer.getId());
            printer.write(object, appender, typeResolver);
        }
    }
}
