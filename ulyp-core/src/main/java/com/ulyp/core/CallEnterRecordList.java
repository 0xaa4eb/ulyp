package com.ulyp.core;

import com.google.protobuf.ByteString;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderType;
import com.ulyp.core.recorders.bytes.BinaryOutputForEnterRecordImpl;
import com.ulyp.transport.TCallEnterRecordDecoder;
import com.ulyp.transport.TCallEnterRecordEncoder;

/**
 * Off-heap list with all call enter records
 */
public class CallEnterRecordList extends AbstractBinaryEncodedList<TCallEnterRecordEncoder, TCallEnterRecordDecoder> {

    private final BinaryOutputForEnterRecordImpl binaryOutput = new BinaryOutputForEnterRecordImpl();

    public CallEnterRecordList() {
    }

    public CallEnterRecordList(ByteString bytes) {
        super(bytes);
    }

    public void add(
            long callId,
            long methodId,
            TypeResolver typeResolver,
            ObjectRecorder[] recorders,
            Object callee,
            Object[] args)
    {
        super.add(encoder -> {
            encoder.callId(callId);
            encoder.methodId(methodId);

            TCallEnterRecordEncoder.ArgumentsEncoder argumentsEncoder = encoder.argumentsCount(args.length);

            for (int i = 0; i < args.length; i++) {
                ObjectRecorder recorder = args[i] != null ? recorders[i] : ObjectRecorderType.NULL_RECORDER.getInstance();

                Type argType = typeResolver.get(args[i]);

                argumentsEncoder = argumentsEncoder.next();
                argumentsEncoder.typeId(argType.getId());
                argumentsEncoder.recorderId(recorder.getId());
                binaryOutput.wrap(encoder);
                try {
                    recorder.write(args[i], binaryOutput, typeResolver);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            ObjectRecorder recorder = callee != null ? ObjectRecorderType.IDENTITY_RECORDER.getInstance() : ObjectRecorderType.NULL_RECORDER.getInstance();

            encoder.calleeTypeId(typeResolver.get(callee).getId());
            encoder.calleeRecorderId(recorder.getId());
            binaryOutput.wrap(encoder);
            try {
                recorder.write(callee, binaryOutput, typeResolver);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
