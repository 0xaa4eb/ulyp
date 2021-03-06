/*
package com.ulyp.core.printers;

import com.ulyp.core.DecodingContext;
import com.ulyp.core.AgentRuntime;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;
import com.ulyp.core.printers.bytes.BinaryOutputAppender;

public class ToStringPrinter extends ObjectBinaryPrinter {

    private static final int TO_STRING_CALL_SUCCESS = 1;
    private static final int TO_STRING_CALL_NULL = 2;
    private static final int TO_STRING_CALL_FAIL = 0;

    protected ToStringPrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(TypeInfo type) {
        if (type.isExactlyJavaLangObject()) {
            return false;
        }

        return type.hasToStringMethod();
    }

    @Override
    public ObjectRepresentation read(TypeInfo objectType, BinaryInput input, DecodingContext decodingContext) {
        long result = input.readLong();
        if (result == TO_STRING_CALL_SUCCESS) {
            // if StringObject representation is returned, then it will look as String literal in UI (green text with double quotes)
            StringObjectRepresentation string = (StringObjectRepresentation) ObjectBinaryPrinterType.STRING_PRINTER.getInstance().read(objectType, input, decodingContext);
            return new PlainObjectRepresentation(objectType, string.getPrintedText());
        } else if (result == TO_STRING_CALL_NULL) {
            return NullObjectRepresentation.getInstance();
        } else {
            return ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance().read(objectType, input, decodingContext);
        }
    }

    @Override
    public void write(Object object, TypeInfo classDescription, BinaryOutput out, AgentRuntime runtime) throws Exception {
        try {
            String printed = object.toString();
            if (printed != null) {
                try (BinaryOutputAppender appender = out.appender()) {
                    appender.append(TO_STRING_CALL_SUCCESS);
                    ObjectBinaryPrinterType.STRING_PRINTER.getInstance().write(printed, appender, runtime);
                }
            } else {
                try (BinaryOutputAppender appender = out.appender()) {
                    appender.append(TO_STRING_CALL_NULL);
                }
            }
        } catch (Throwable e) {
            try (BinaryOutputAppender appender = out.appender()) {
                appender.append(TO_STRING_CALL_FAIL);
                ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance().write(object, appender, runtime);
            }
        }
    }
}
*/
