package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

import java.util.Date;

public class DatePrinter extends ObjectBinaryPrinter {

    private static final String JAVA_UTIL_DATE_CLASS_NAME = Date.class.getName();

    protected DatePrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(TypeInfo type) {
        return type.getName().equals(JAVA_UTIL_DATE_CLASS_NAME);
    }

    @Override
    public ObjectRepresentation read(TypeInfo objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new DateRepresentation(objectType, input.readString());
    }

    @Override
    public void write(Object object, TypeInfo objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        out.writeString(object.toString());
    }
}
