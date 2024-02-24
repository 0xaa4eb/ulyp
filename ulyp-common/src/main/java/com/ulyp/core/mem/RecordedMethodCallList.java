package com.ulyp.core.mem;

import com.ulyp.core.*;
import com.ulyp.core.bytes.PagedMemBinaryOutput;
import com.ulyp.core.serializers.RecordedEnterMethodCallSerializer;
import com.ulyp.core.serializers.RecordedExitMethodCallSerializer;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

/**
 * A list of serialized {@link RecordedMethodCall} instances
 */
public class RecordedMethodCallList {

    public static final byte ENTER_METHOD_CALL_ID = 1;
    public static final int WIRE_ID = 2;

    private final OutputBinaryList out;

    @TestOnly
    public RecordedMethodCallList(int recordingId, OutputBinaryList writeBinaryList) {
        this.out = writeBinaryList;

        writeBinaryList.add(out -> out.write(recordingId));
    }

    public RecordedMethodCallList(int recordingId, MemPageAllocator pageAllocator) {
        this.out = new OutputBinaryList(WIRE_ID, new PagedMemBinaryOutput(pageAllocator));

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
        OutputBinaryList.Writer writer = out.writer();
        RecordedExitMethodCallSerializer.instance.serializeExitMethodCall(writer, callId, typeResolver, thrown, returnValue, nanoTime);
        writer.commit();
    }

    public void addEnterMethodCall(int callId, int methodId, TypeResolver typeResolver, Object callee, @Nullable Object[] args) {
        addEnterMethodCall(callId, methodId, typeResolver, callee, args, -1L);
    }

    public void addEnterMethodCall(int callId, int methodId, TypeResolver typeResolver, Object callee, @Nullable Object[] args, long nanoTime) {
        OutputBinaryList.Writer writer = out.writer();
        RecordedEnterMethodCallSerializer.instance.serializeEnterMethodCall(writer, callId, methodId, typeResolver, callee, args, nanoTime);
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

    public OutputBinaryList toBytes() {
        return out;
    }
}
