package com.ulyp.storage.reader;

import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.util.Preconditions;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.util.ByAddressFileReader;
import com.ulyp.transport.*;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

class RecordedMethodCallDataReader implements Closeable {

    private final ByAddressFileReader reader;

    public RecordedMethodCallDataReader(File file) {
        this.reader = new ByAddressFileReader(file);
    }

    public RecordedEnterMethodCall readEnterMethodCall(long addr) {
        try {
            byte[] bytes = reader.readBytes(addr, 8 * 1024);
            UnsafeBuffer mem = new UnsafeBuffer(bytes);

            BinaryDataDecoder decoder = new BinaryDataDecoder();
            decoder.wrap(mem, 0, BinaryDataDecoder.BLOCK_LENGTH, 0);
            UnsafeBuffer buffer = new UnsafeBuffer();
            decoder.wrapValue(buffer);
            Preconditions.checkState(decoder.id() == RecordedMethodCallList.ENTER_METHOD_CALL_ID, "");
            BinaryRecordedEnterMethodCallDecoder enterMethodCallDecoder = new BinaryRecordedEnterMethodCallDecoder();
            enterMethodCallDecoder.wrap(buffer, 0, BinaryRecordedEnterMethodCallEncoder.BLOCK_LENGTH, 0);
            return RecordedEnterMethodCall.deserialize(enterMethodCallDecoder);
        } catch (IOException e) {
            throw new StorageException(
                    "Could not read " + RecordedEnterMethodCall.class.getSimpleName() +
                            " at address " + addr +
                            " in file " + reader
            );
        }
    }

    public RecordedExitMethodCall readExitMethodCall(long addr) {
        try {
            byte[] bytes = reader.readBytes(addr, 8 * 1024);
            UnsafeBuffer mem = new UnsafeBuffer(bytes);

            BinaryDataDecoder decoder = new BinaryDataDecoder();
            decoder.wrap(mem, 0, BinaryDataDecoder.BLOCK_LENGTH, 0);
            UnsafeBuffer buffer = new UnsafeBuffer();
            decoder.wrapValue(buffer);
            Preconditions.checkState(decoder.id() == RecordedMethodCallList.EXIT_METHOD_CALL_ID, "");
            BinaryRecordedExitMethodCallDecoder exitMethodCallDecoder = new BinaryRecordedExitMethodCallDecoder();
            exitMethodCallDecoder.wrap(buffer, 0, BinaryRecordedExitMethodCallDecoder.BLOCK_LENGTH, 0);
            return RecordedExitMethodCall.deserialize(exitMethodCallDecoder);
        } catch (IOException e) {
            throw new StorageException(
                    "Could not read " + RecordedExitMethodCall.class.getSimpleName() +
                            " at address " + addr +
                            " in file " + reader
            );
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}