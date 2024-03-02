package com.ulyp.storage.reader;

import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.core.Type;
import com.ulyp.core.bytes.DirectBytesIn;
import com.ulyp.core.repository.ReadableRepository;
import com.ulyp.core.serializers.RecordedEnterMethodCallSerializer;
import com.ulyp.core.serializers.RecordedExitMethodCallSerializer;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.util.ByAddressFileReader;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

class RecordedMethodCallDataReader implements Closeable {

    private final ByAddressFileReader reader;

    public RecordedMethodCallDataReader(File file) {
        this.reader = new ByAddressFileReader(file);
    }

    public RecordedEnterMethodCall readEnterMethodCall(long addr, ReadableRepository<Integer, Type> typeRepository) {
        try {
            byte[] bytes = reader.readBytes(addr, 8 * 1024);
            DirectBytesIn input = new DirectBytesIn(new UnsafeBuffer(bytes));
            input.readByte();
            return RecordedEnterMethodCallSerializer.deserialize(input, typeRepository);
        } catch (IOException e) {
            throw new StorageException(
                    "Could not read " + RecordedEnterMethodCall.class.getSimpleName() +
                            " at address " + addr +
                            " in file " + reader
            );
        }
    }

    public RecordedExitMethodCall readExitMethodCall(long addr, ReadableRepository<Integer, Type> typeRepository) {
        try {
            byte[] bytes = reader.readBytes(addr, 8 * 1024);
            DirectBytesIn input = new DirectBytesIn(new UnsafeBuffer(bytes));
            input.readByte(); // TODO this is ugly
            return RecordedExitMethodCallSerializer.deserialize(input, typeRepository);
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
