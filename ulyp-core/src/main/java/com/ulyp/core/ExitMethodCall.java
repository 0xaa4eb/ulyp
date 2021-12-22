package com.ulyp.core;

import com.ulyp.transport.BinaryExitMethodCallDecoder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class ExitMethodCall extends  MethodCall {

    private final RecordedObject returnValue;
    private final boolean thrown;

    public static MethodCall deserialize(BinaryExitMethodCallDecoder decoder) {

        byte[] returnValueBytes = new byte[decoder.returnValueBytesLength()];
        decoder.getReturnValueBytes(returnValueBytes, 0, returnValueBytes.length);

        return ExitMethodCall.builder()
                .returnValue(
                        RecordedObject.builder()
                                .recorderId(decoder.returnValuePrinterId())
                                .typeId(decoder.returnValueTypeId())
                                .value(returnValueBytes)
                                .build()
                )
                .callId(decoder.callId())
                .methodId(decoder.methodId())
                .build();
    }
}
