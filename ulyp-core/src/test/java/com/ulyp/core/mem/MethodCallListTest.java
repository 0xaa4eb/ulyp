package com.ulyp.core.mem;

import com.ulyp.core.*;
import com.ulyp.core.printers.ObjectRecorder;
import com.ulyp.core.printers.ObjectBinaryPrinterType;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class MethodCallListTest {

    public static class A {
        public String convert(int x) {
            return String.valueOf(x);
        }
    }

    private final MethodCallList list = new MethodCallList();
    private final ReflectionBasedTypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    public void testAddAndIterate() {

        Type type = typeResolver.get(A.class);

        Method method = Method.builder()
                .id(5L)
                .name("convert")
                .declaringType(type)
                .paramPrinters(new ObjectRecorder[]{ObjectBinaryPrinterType.ANY_NUMBER_PRINTER.getInstance()})
                .returnValuePrinter(ObjectBinaryPrinterType.STRING_PRINTER.getInstance())
                .build();

        list.addEnterMethodCall(
                134L,
                method,
                typeResolver,
                new A(),
                new Object[]{5}
        );

        list.addExitMethodCall(
                134L,
                method,
                typeResolver,
                false,
                "ABC"
        );


        List<MethodCall> calls = list.stream().collect(Collectors.toList());

        EnterMethodCall enterMethodCall = (EnterMethodCall) calls.get(0);

        Assert.assertEquals(134L, enterMethodCall.getCallId());

        ExitMethodCall exitMethodCall = (ExitMethodCall) calls.get(1);

        Assert.assertEquals(134L, exitMethodCall.getCallId());
    }
}