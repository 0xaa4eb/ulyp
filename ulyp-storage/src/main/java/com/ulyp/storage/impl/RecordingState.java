package com.ulyp.storage.impl;

import com.ulyp.core.*;
import com.ulyp.core.impl.InMemoryIndex;
import com.ulyp.core.impl.Index;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.storage.Repository;
import com.ulyp.transport.BinaryDataDecoder;
import com.ulyp.transport.BinaryRecordedEnterMethodCallDecoder;
import com.ulyp.transport.BinaryRecordedEnterMethodCallEncoder;
import com.ulyp.transport.BinaryRecordedExitMethodCallDecoder;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.IOException;

class RecordingState {

    private final int id;
    private final ByAddressFileReader reader;
    private final Repository<RecordedCallState> index = new InMemoryRepository<>();
    private final MemCallStack memCallStack = new MemCallStack();

    RecordingState(int id, ByAddressFileReader input) {
        this.id = id;
        this.reader = input;
    }

    void onRecordedCalls(long fileAddr, RecordedMethodCallList calls) {

        AddressableItemIterator<RecordedMethodCall> iterator = calls.iterator();
        while (iterator.hasNext()) {
            RecordedMethodCall value = iterator.next();
            long relativeAddress = iterator.address();
            if (value instanceof RecordedEnterMethodCall) {
                RecordedCallState callState = new RecordedCallState(
                        value.getCallId(),
                        fileAddr + relativeAddress
                );
                memCallStack.push(callState);
            } else {

                RecordedCallState callState = memCallStack.pop();
                callState.setExitMethodCallAddr(fileAddr + relativeAddress);
                index.store(callState.getCallId(), callState);
            }
        }
    }

    RecordedCallState getState(long callId) {
        RecordedCallState callState = memCallStack.get(callId);
        if (callState != null) {
            return index.get(callId);
        }
        return null;
    }

    private void test(long addr) {
        try {
            byte[] bytes = reader.readBytes(addr, 8 * 1024);
            UnsafeBuffer mem = new UnsafeBuffer(bytes);

            BinaryDataDecoder decoder = new BinaryDataDecoder();
            decoder.wrap(mem, 0, BinaryDataDecoder.BLOCK_LENGTH, 0);
            UnsafeBuffer buffer = new UnsafeBuffer();
            decoder.wrapValue(buffer);
            if (decoder.id() == BinaryRecordedEnterMethodCallEncoder.TEMPLATE_ID) {
                BinaryRecordedEnterMethodCallDecoder enterMethodCallDecoder = new BinaryRecordedEnterMethodCallDecoder();
                enterMethodCallDecoder.wrap(buffer, 0, BinaryRecordedEnterMethodCallEncoder.BLOCK_LENGTH, 0);
                RecordedMethodCall value = RecordedEnterMethodCall.deserialize(enterMethodCallDecoder);
            } else {
                BinaryRecordedExitMethodCallDecoder exitMethodCallDecoder = new BinaryRecordedExitMethodCallDecoder();
                exitMethodCallDecoder.wrap(buffer, 0, BinaryRecordedExitMethodCallDecoder.BLOCK_LENGTH, 0);
                RecordedMethodCall value = RecordedExitMethodCall.deserialize(exitMethodCallDecoder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public void update(RecordingMetadata metadata) {
        // TODO
    }
}
