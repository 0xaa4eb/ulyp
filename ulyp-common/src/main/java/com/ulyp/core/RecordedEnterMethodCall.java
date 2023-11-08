package com.ulyp.core;

import com.ulyp.transport.BinaryRecordedEnterMethodCallDecoder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder
public class RecordedEnterMethodCall extends RecordedMethodCall {

    private final int methodId;
    private final RecordedObject callee;
    private final List<RecordedObject> arguments;

    public static RecordedEnterMethodCall deserialize(BinaryRecordedEnterMethodCallDecoder decoder) {

        List<RecordedObject> arguments = new ArrayList<>();

        decoder.arguments().forEach(
                argumentsDecoder -> {
                    byte[] bytes = new byte[argumentsDecoder.bytesLength()];
                    argumentsDecoder.getBytes(bytes, 0, bytes.length);
                    arguments.add(RecordedObject.builder()
                            .value(bytes)
                            .typeId(argumentsDecoder.typeId())
                            .recorderId(argumentsDecoder.recorderId())
                            .build()
                    );
                }
        );

        byte[] calleeBytes = new byte[decoder.calleeBytesLength()];
        decoder.getCalleeBytes(calleeBytes, 0, calleeBytes.length);
        RecordedObject callee = RecordedObject.builder()
                .recorderId(decoder.calleeRecorderId())
                .typeId(decoder.calleeTypeId())
                .value(calleeBytes)
                .build();

        return RecordedEnterMethodCall.builder()
                .callId(decoder.callId())
                .methodId(decoder.methodId())
                .callee(callee)
                .arguments(arguments)
                .build();
    }
}
