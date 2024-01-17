package com.ulyp.core.mem;

import com.ulyp.core.*;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.RecorderChooser;
import com.ulyp.core.recorders.bytes.BufferBinaryOutput;
import org.agrona.ExpandableDirectByteBuffer;
import org.jetbrains.annotations.TestOnly;

/**
 * A list of serialized {@link RecordedMethodCall} instances
 */
public class RecordedMethodCallList {

    public static final byte ENTER_METHOD_CALL_ID = 1;
    public static final byte EXIT_METHOD_CALL_ID = 2;
    public static final int WIRE_ID = 2;

    private final BinaryList.Out out;

    @TestOnly
    public RecordedMethodCallList(int recordingId, BinaryList.Out writeBinaryList) {
        this.out = writeBinaryList;

        writeBinaryList.add(out -> out.write(recordingId));
    }

    public RecordedMethodCallList(int recordingId) {
        this.out = new BinaryList.Out(WIRE_ID, new BufferBinaryOutput(new ExpandableDirectByteBuffer()));

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
        out.add(out -> {
            out.write(EXIT_METHOD_CALL_ID);
            out.write(callId);
            out.write(thrown);
            out.write(nanoTime);

            Type type = typeResolver.get(returnValue);
            out.write(type.getId());

            ObjectRecorder recorderHint = type.getRecorderHint();
            if (returnValue != null && recorderHint == null) {
                recorderHint = RecorderChooser.getInstance().chooseForType(returnValue.getClass());
                type.setRecorderHint(recorderHint);
            }

            ObjectRecorder recorder = returnValue != null ?
                    (thrown ? ObjectRecorderRegistry.THROWABLE_RECORDER.getInstance() : recorderHint) :
                    ObjectRecorderRegistry.NULL_RECORDER.getInstance();

            out.write(recorder.getId());

            try {
                recorder.write(returnValue, out, typeResolver);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void addEnterMethodCall(int callId, Method method, TypeResolver typeResolver, Object callee, Object[] args) {
        addEnterMethodCall(callId, method, typeResolver, callee, args, -1L);
    }

    public void addEnterMethodCall(int callId, Method method, TypeResolver typeResolver, Object callee, Object[] args, long nanoTime) {
        out.add(out -> {
            out.write(ENTER_METHOD_CALL_ID);

            out.write(callId);
            out.write(method.getId());
            out.write(nanoTime);
            out.write(args.length);

            for (int i = 0; i < args.length; i++) {
                Object argValue = args[i];
                Type argType = typeResolver.get(argValue);
                ObjectRecorder recorderHint = argType.getRecorderHint();
                if (argValue != null && recorderHint == null) {
                    recorderHint = RecorderChooser.getInstance().chooseForType(argValue.getClass());
                    argType.setRecorderHint(recorderHint);
                }

                ObjectRecorder recorder = argValue != null ? recorderHint : ObjectRecorderRegistry.NULL_RECORDER.getInstance();

                out.write(argType.getId());
                out.write(recorder.getId());
                try {
                    recorder.write(argValue, out, typeResolver);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            ObjectRecorder recorder = callee != null ? ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance() : ObjectRecorderRegistry.NULL_RECORDER.getInstance();

            out.write(typeResolver.get(callee).getId());
            out.write(recorder.getId());
            try {
                recorder.write(callee, out, typeResolver);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
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
