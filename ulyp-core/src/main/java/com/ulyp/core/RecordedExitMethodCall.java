package com.ulyp.core;

import com.ulyp.transport.BinaryRecordedExitMethodCallDecoder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class RecordedExitMethodCall extends RecordedMethodCall {

    private final RecordedObject returnValue;
    private final boolean thrown;

    public static RecordedExitMethodCall deserialize(BinaryRecordedExitMethodCallDecoder decoder) {

        byte[] returnValueBytes = new byte[decoder.returnValueBytesLength()];
        decoder.getReturnValueBytes(returnValueBytes, 0, returnValueBytes.length);

        return RecordedExitMethodCall.builder()
                .returnValue(
                        RecordedObject.builder()
                                .recorderId(decoder.returnValueRecorderId())
                                .typeId(decoder.returnValueTypeId())
                                .value(returnValueBytes)
                                .build()
                )
                .recordingId(decoder.recordingId())
                .callId(decoder.callId())
                .methodId(decoder.methodId())
                .build();
    }
}
