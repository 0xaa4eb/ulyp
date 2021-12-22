package com.ulyp.core;

import com.ulyp.transport.BinaryEnterMethodCallDecoder;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
public class EnterMethodCall extends MethodCall {

    private final RecordedObject callee;
    private final List<RecordedObject> arguments;

    public static MethodCall deserialize(BinaryEnterMethodCallDecoder decoder) {

        List<RecordedObject> arguments = new ArrayList<>();

        decoder.arguments().forEach(
                argumentsDecoder -> {
                    byte[] bytes = new byte[argumentsDecoder.bytesLength()];
                    argumentsDecoder.getBytes(bytes, 0, bytes.length);
                    arguments.add(RecordedObject.builder().value(bytes).typeId(argumentsDecoder.typeId()).recorderId(argumentsDecoder.printerId()).build());
                }
        );

        byte[] returnValueBytes = new byte[decoder.calleeBytesLength()];
        decoder.getCalleeBytes(returnValueBytes, 0, returnValueBytes.length);
        RecordedObject callee = RecordedObject.builder().build();

        return EnterMethodCall.builder()
                .callId(decoder.callId())
                .methodId(decoder.methodId())
                .callee(callee)
                .arguments(arguments)
                .build();
    }
}
