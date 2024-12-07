package com.ulyp.core.mem;

import com.ulyp.core.RecordedMethodCall;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.PagedMemBytesOut;
import com.ulyp.core.serializers.RecordedEnterMethodCallSerializer;
import com.ulyp.core.serializers.RecordedExitMethodCallSerializer;
import org.jetbrains.annotations.TestOnly;

/**
 * A list of serialized {@link RecordedMethodCall} instances
 */
public class SerializedRecordedMethodCallList {

    public static final byte ENTER_METHOD_CALL_ID = 1;
    public static final int WIRE_ID = 2;

    private final OutputBytesList out;

    @TestOnly
    public SerializedRecordedMethodCallList(int recordingId, OutputBytesList writeBinaryList) {
        this.out = writeBinaryList;

        writeBinaryList.add(out -> out.write(recordingId));
    }

    public SerializedRecordedMethodCallList(int recordingId, MemPageAllocator pageAllocator) {
        this.out = new OutputBytesList(WIRE_ID, new PagedMemBytesOut(pageAllocator));

        out.add(out -> out.write(recordingId));
    }

    public void addExitMethodCall(int callId, TypeResolver typeResolver, Object returnValue) {
        addExitMethodCall(callId, typeResolver, false, returnValue, -1);
    }

    public void addExitMethodCall(int callId, TypeResolver typeResolver, Object returnValue, long nanoTime) {
        addExitMethodCall(callId, typeResolver, false, returnValue, nanoTime);
    }

    public void addExitMethodThrow(int callId, TypeResolver typeResolver, Object throwObject, long nanoTime) {
        addExitMethodCall(callId, typeResolver, true, throwObject, nanoTime);
    }

    private void addExitMethodCall(int callId, TypeResolver typeResolver, boolean thrown, Object returnValue, long nanoTime) {
        OutputBytesList.Writer writer = out.writer();
        RecordedExitMethodCallSerializer.instance.serializeExitMethodCall(writer, callId, typeResolver, thrown, returnValue, nanoTime);
        writer.commit();
    }

    public void addEnterMethodCall(int methodId, TypeResolver typeResolver, Object callee, Object[] args) {
        addEnterMethodCall(methodId, typeResolver, callee, args, -1L);
    }

    public void addEnterMethodCall(int methodId, TypeResolver typeResolver, Object callee, Object[] args, long nanoTime) {
        OutputBytesList.Writer writer = out.writer();
        RecordedEnterMethodCallSerializer.instance.serializeEnterMethodCall(writer, methodId, typeResolver, callee, args, nanoTime);
        writer.commit();
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

    public OutputBytesList toBytes() {
        return out;
    }
}
