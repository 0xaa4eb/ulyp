package com.ulyp.core;

import com.google.protobuf.ByteString;
import com.ulyp.transport.BooleanType;
import com.ulyp.transport.TMethodInfoDecoder;
import com.ulyp.transport.TMethodInfoEncoder;

// Flexible SBE wrapper
public class MethodInfoList extends AbstractBinaryEncodedList<TMethodInfoEncoder, TMethodInfoDecoder> {

    public MethodInfoList() {
    }

    public MethodInfoList(ByteString bytes) {
        super(bytes);
    }

    public void add(Method method) {
        super.add(encoder -> {
            encoder.id(method.getId());
            encoder.returnsSomething(method.returnsSomething() ? BooleanType.T : BooleanType.F);
            encoder.staticFlag(method.isStatic() ? BooleanType.T : BooleanType.F);
            encoder.constructor(method.isConstructor() ? BooleanType.T : BooleanType.F);

            // TODO delete
            encoder.parameterNamesCount(0);

            encoder.className(method.getDeclaringType().getName());
            encoder.methodName(method.getName());
        });
    }
}
