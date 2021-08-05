package com.ulyp.agent.util;

import com.ulyp.core.Method;
import com.ulyp.core.Type;
import com.ulyp.core.printers.ObjectBinaryPrinter;
import com.ulyp.core.printers.ObjectBinaryPrinterType;
import com.ulyp.core.printers.Printers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ByteBuddyMethodResolver {

    private static final AtomicLong counter = new AtomicLong();
    private final ByteBuddyTypeResolver typeResolver = new ByteBuddyTypeResolver();

    public Method resolve(MethodDescription description) {
        try {
            boolean returns = !description.getReturnType().asGenericType().equals(TypeDescription.Generic.VOID);
            List<Type> parameters = description.getParameters().asTypeList().stream().map(typeResolver::resolve).collect(Collectors.toList());
            Type returnType = typeResolver.resolve(description.getReturnType());
            Type declaringType = typeResolver.resolve(description.getDeclaringType().asGenericType());

            ObjectBinaryPrinter[] paramPrinters = Printers.getInstance().determinePrintersForParameterTypes(parameters);
            ObjectBinaryPrinter returnValuePrinter = description.isConstructor() ?
                    ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance() :
                    Printers.getInstance().determinePrinterForReturnType(returnType);

            return Method.builder()
                    .id(counter.incrementAndGet())
                    .name(description.isConstructor() ? "<init>" : description.getActualName())
                    .isConstructor(description.isConstructor())
                    .isStatic(description.isStatic())
                    .returnsSomething(returns)
                    .paramPrinters(paramPrinters)
                    .returnValuePrinter(returnValuePrinter)
                    .declaringType(declaringType)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
