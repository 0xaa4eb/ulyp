package com.ulyp.agent.util;

import com.ulyp.core.Method;
import com.ulyp.core.Type;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.printers.ObjectBinaryPrinter;
import com.ulyp.core.printers.ObjectBinaryPrinterType;
import com.ulyp.core.printers.Printers;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Converts byte buddy method description to internal model
 */
@Slf4j
public class ByteBuddyMethodResolver {

    private static final AtomicLong idGenerator = new AtomicLong();

    private final ByteBuddyTypeResolver typeResolver = new ByteBuddyTypeResolver();

    public Method resolve(MethodDescription description) {
        boolean returns = !description.getReturnType().asGenericType().equals(TypeDescription.Generic.VOID);
        List<Type> parameters = description.getParameters().asTypeList().stream().map(typeResolver::resolve).collect(Collectors.toList());
        Type returnType = typeResolver.resolve(description.getReturnType());
        Type declaringType = typeResolver.resolve(description.getDeclaringType().asGenericType());

        ObjectBinaryPrinter[] paramPrinters = Printers.getInstance().determinePrintersForParameterTypes(parameters);
        ObjectBinaryPrinter returnValuePrinter = description.isConstructor() ?
                ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance() :
                Printers.getInstance().determinePrinterForReturnType(returnType);

        Method resolved = Method.builder()
                .id(idGenerator.incrementAndGet())
                .name(description.isConstructor() ? "<init>" : description.getActualName())
                .isConstructor(description.isConstructor())
                .isStatic(description.isStatic())
                .returnsSomething(returns)
                .paramPrinters(paramPrinters)
                .returnValuePrinter(returnValuePrinter)
                .declaringType(declaringType)
                .build();

        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Resolved {} to {}", description, resolved);
        }
        return resolved;
    }
}
