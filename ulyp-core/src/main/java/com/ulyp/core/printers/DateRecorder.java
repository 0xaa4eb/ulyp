package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

import java.util.Date;

public class DateRecorder extends ObjectBinaryRecorder {

    private static final String JAVA_UTIL_DATE_CLASS_NAME = Date.class.getName();

    protected DateRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.getName().equals(JAVA_UTIL_DATE_CLASS_NAME);
    }

    @Override
    public ObjectRepresentation read(Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new DateRepresentation(objectType, input.readString());
    }

    @Override
    public void write(Object object, Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        out.writeString(object.toString());
    }
}
