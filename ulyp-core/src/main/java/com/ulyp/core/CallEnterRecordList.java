package com.ulyp.core;

import com.google.protobuf.ByteString;
import com.ulyp.core.printers.ObjectBinaryRecorder;
import com.ulyp.core.printers.ObjectBinaryPrinterType;
import com.ulyp.core.printers.bytes.BinaryOutputForEnterRecordImpl;
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
            ObjectBinaryRecorder[] printers,
            Object callee,
            Object[] args)
    {
        super.add(encoder -> {
            encoder.callId(callId);
            encoder.methodId(methodId);

            TCallEnterRecordEncoder.ArgumentsEncoder argumentsEncoder = encoder.argumentsCount(args.length);

            for (int i = 0; i < args.length; i++) {
                ObjectBinaryRecorder printer = args[i] != null ? printers[i] : ObjectBinaryPrinterType.NULL_PRINTER.getInstance();

                Type argType = typeResolver.get(args[i]);

                argumentsEncoder = argumentsEncoder.next();
                argumentsEncoder.typeId(argType.getId());
                argumentsEncoder.printerId(printer.getId());
                binaryOutput.wrap(encoder);
                try {
                    printer.write(args[i], binaryOutput, typeResolver);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            ObjectBinaryRecorder printer = callee != null ? ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance() : ObjectBinaryPrinterType.NULL_PRINTER.getInstance();

            encoder.calleeTypeId(typeResolver.get(callee).getId());
            encoder.calleePrinterId(printer.getId());
            binaryOutput.wrap(encoder);
            try {
                printer.write(callee, binaryOutput, typeResolver);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
