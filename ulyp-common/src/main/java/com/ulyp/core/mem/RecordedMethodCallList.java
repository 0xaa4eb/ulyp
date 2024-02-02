package com.ulyp.core.mem;

import com.ulyp.core.*;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.MemBinaryOutput;
import com.ulyp.core.serializers.RecordedEnterMethodCallSerializer;
import com.ulyp.core.serializers.RecordedExitMethodCallSerializer;
import org.agrona.concurrent.UnsafeBuffer;
import org.jetbrains.annotations.TestOnly;

/**
 * A list of serialized {@link RecordedMethodCall} instances
 */
public class RecordedMethodCallList {

    public static final byte ENTER_METHOD_CALL_ID = 1;
    public static final int WIRE_ID = 2;

    private final BinaryList.Out out;

    @TestOnly
    public RecordedMethodCallList(int recordingId, BinaryList.Out writeBinaryList) {
        this.out = writeBinaryList;

        writeBinaryList.add(out -> out.write(recordingId));
    }

    public RecordedMethodCallList(int recordingId) {
        this.out = new BinaryList.Out(WIRE_ID, new MemBinaryOutput(new MemPageAllocator() {

            @Override
            public MemPage allocate() {
                return new MemPage(0, new UnsafeBuffer(new byte[MemPool.PAGE_SIZE]));
            }

            @Override
            public void deallocate(MemPage page) {

            }
        }));

        out.add(out -> out.write(recordingId));
    }

    public RecordedMethodCallList(int recordingId, BinaryOutput binaryOutput) {
        this.out = new BinaryList.Out(WIRE_ID, binaryOutput);

        out.add(out -> out.write(recordingId));
    }

    public void addExitMethodCall(int callId, TypeResolver typeResolver, Object returnValue) {
        addExitMethodCall(callId, typeResolver, false, returnValue, -1);
    }

    public void addExitMethodCall(int callId, TypeResolver typeResolver, Object returnValue, long nanoTime) {
        addExitMethodCall(callId, typeResolver, false, returnValue, nanoTime);
    }

    public void addExitMethodThrow(int callId, TypeResolver typeResolver, Object throwObject) {
        addExitMethodCall(callId, typeResolver, true, throwObject, -1L);
    }

    public void addExitMethodThrow(int callId, TypeResolver typeResolver, Object throwObject, long nanoTime) {
        addExitMethodCall(callId, typeResolver, true, throwObject, nanoTime);
    }

    private void addExitMethodCall(int callId, TypeResolver typeResolver, boolean thrown, Object returnValue, long nanoTime) {
        out.add(out -> RecordedExitMethodCallSerializer.instance.serializeExitMethodCall(out, callId, typeResolver, thrown, returnValue, nanoTime));
    }

    public void addEnterMethodCall(int callId, int methodId, TypeResolver typeResolver, Object callee, Object[] args) {
        addEnterMethodCall(callId, methodId, typeResolver, callee, args, -1L);
    }

    public void addEnterMethodCall(int callId, int methodId, TypeResolver typeResolver, Object callee, Object[] args, long nanoTime) {
        out.add(out -> RecordedEnterMethodCallSerializer.instance.serializeEnterMethodCall(out, callId, methodId, typeResolver, callee, args, nanoTime));
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return out.size() - 1;
    }

    public int bytesWritten() {
        return out.bytesWritten();
    }

    public BinaryList.Out toBytes() {
        return out;
    }
}
