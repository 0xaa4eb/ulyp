package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;
import com.ulyp.core.printers.bytes.BinaryOutputAppender;
import com.ulyp.core.util.ClassMatcher;

import java.util.HashSet;
import java.util.Set;

public class ToStringPrinter extends ObjectBinaryPrinter {

    private static final int TO_STRING_CALL_SUCCESS = 1;
    private static final int TO_STRING_CALL_FAIL = 0;

    private final Set<ClassMatcher> classesToPrintWithToString = new HashSet<>();

    protected ToStringPrinter(byte id) {
        super(id);
    }

    public void addClassNamesSupportPrinting(Set<ClassMatcher> classNames) {
        this.classesToPrintWithToString.addAll(classNames);
    }

    @Override
    boolean supports(Type type) {
        return classesToPrintWithToString.stream().anyMatch(x -> x.matches(type));
    }

    @Override
    public ObjectRepresentation read(Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        int result = input.readInt();
        if (result == TO_STRING_CALL_SUCCESS) {
            int identityHashCode = input.readInt();
            ObjectRepresentation printed = input.readObject(typeResolver);
            return new ToStringPrintedRepresentation(printed, objectType, identityHashCode);
        } else {
            return ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance().read(objectType, input, typeResolver);
        }
    }

    @Override
    public void write(Object object, Type classDescription, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        try {
            String printed = object.toString();

            try (BinaryOutputAppender appender = out.appender()) {
                appender.append(TO_STRING_CALL_SUCCESS);
                appender.append(System.identityHashCode(object));
                appender.append(printed, typeResolver);
            }
        } catch (Throwable e) {
            try (BinaryOutputAppender appender = out.appender()) {
                appender.append(TO_STRING_CALL_FAIL);
                ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance().write(object, appender, typeResolver);
            }
        }
    }
}
