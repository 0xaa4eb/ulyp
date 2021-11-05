package com.ulyp.core;

import com.google.protobuf.ByteString;
import com.ulyp.core.printers.ObjectBinaryRecorder;
import com.ulyp.core.printers.ObjectBinaryPrinterType;
import com.ulyp.core.printers.bytes.BinaryOutputForExitRecordImpl;
import com.ulyp.transport.BooleanType;
import com.ulyp.transport.TCallExitRecordDecoder;
import com.ulyp.transport.TCallExitRecordEncoder;

/**
 * Off-heap list with all call exit records
 */
public class CallExitRecordList extends AbstractBinaryEncodedList<TCallExitRecordEncoder, TCallExitRecordDecoder> {

    private final BinaryOutputForExitRecordImpl binaryOutput = new BinaryOutputForExitRecordImpl();

    public CallExitRecordList() {
    }

    public CallExitRecordList(ByteString bytes) {
        super(bytes);
    }

    public void add(
            long callId,
            long methodId,
            TypeResolver typeResolver,
            boolean thrown,
            ObjectBinaryRecorder returnValuePrinter,
            Object returnValue)
    {
        super.add(encoder -> {
            encoder.callId(callId);
            encoder.methodId(methodId);
            encoder.thrown(thrown ? BooleanType.T : BooleanType.F);
            Type classDescription = typeResolver.get(returnValue);
            encoder.returnTypeId(classDescription.getId());

            ObjectBinaryRecorder printer = returnValue != null ?
                    returnValuePrinter :
                    ObjectBinaryPrinterType.NULL_PRINTER.getInstance();

            encoder.returnPrinterId(printer.getId());
            binaryOutput.wrap(encoder);
            try {
                printer.write(returnValue, binaryOutput, typeResolver);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
