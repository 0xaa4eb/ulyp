package com.ulyp.storage.impl;

import com.google.common.base.Preconditions;
import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.storage.StorageException;
import com.ulyp.transport.*;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

class DataReader implements Closeable {

    private final ByAddressFileReader reader;

    public DataReader(File file) {
        this.reader = new ByAddressFileReader(file);
    }

    RecordedEnterMethodCall readEnterMethodCall(long addr) {
        try {
            byte[] bytes = reader.readBytes(addr, 8 * 1024);
            UnsafeBuffer mem = new UnsafeBuffer(bytes);

            BinaryDataDecoder decoder = new BinaryDataDecoder();
            decoder.wrap(mem, 0, BinaryDataDecoder.BLOCK_LENGTH, 0);
            UnsafeBuffer buffer = new UnsafeBuffer();
            decoder.wrapValue(buffer);
            Preconditions.checkState(decoder.id() == BinaryRecordedEnterMethodCallEncoder.TEMPLATE_ID, "");
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

    RecordedExitMethodCall readExitMethodCall(long addr) {
        try {
            byte[] bytes = reader.readBytes(addr, 8 * 1024);
            UnsafeBuffer mem = new UnsafeBuffer(bytes);

            BinaryDataDecoder decoder = new BinaryDataDecoder();
            decoder.wrap(mem, 0, BinaryDataDecoder.BLOCK_LENGTH, 0);
            UnsafeBuffer buffer = new UnsafeBuffer();
            decoder.wrapValue(buffer);
            Preconditions.checkState(decoder.id() == BinaryRecordedExitMethodCallEncoder.TEMPLATE_ID, "");
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
