package com.ulyp.storage.impl;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.process.ProcessInfo;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.StorageWriter;
import com.ulyp.transport.BinaryProcessMetadataEncoder;
import com.ulyp.transport.BinaryRecordingMetadataEncoder;
import org.agrona.MutableDirectBuffer;

import java.io.File;
import java.io.IOException;

public class FileStorageWriter implements StorageWriter {

    private final BinaryListFileWriter writer;

    public FileStorageWriter(File file) throws StorageException {
        try {
            this.writer = new BinaryListFileWriter(file);
        } catch (IOException e) {
            throw new StorageException("Could not build storage for file " + file, e);
        }
    }

    @Override
    public synchronized void write(ProcessMetadata processMetadata) {
        BinaryList binaryList = new BinaryList(ProcessMetadata.WIRE_ID);
        binaryList.add(
                encoder -> {
                    MutableDirectBuffer wrappedBuffer = encoder.buffer();
                    int headerLength = 4;
                    int limit = encoder.limit();
                    BinaryProcessMetadataEncoder binaryMetadataEncoder = new BinaryProcessMetadataEncoder();
                    binaryMetadataEncoder.wrap(wrappedBuffer, limit + headerLength);
                    processMetadata.serialize(binaryMetadataEncoder);
                    int typeSerializedLength = binaryMetadataEncoder.encodedLength();
                    encoder.limit(limit + headerLength + typeSerializedLength);
                    wrappedBuffer.putInt(limit, typeSerializedLength, java.nio.ByteOrder.LITTLE_ENDIAN);
                }
        );
        writer.append(binaryList);
    }

    @Override
    public synchronized void write(RecordingMetadata recordingMetadata) {
        BinaryList binaryList = new BinaryList(RecordingMetadata.WIRE_ID);
        binaryList.add(
                encoder -> {
                    MutableDirectBuffer wrappedBuffer = encoder.buffer();
                    int headerLength = 4;
                    int limit = encoder.limit();
                    BinaryRecordingMetadataEncoder binaryMetadataEncoder = new BinaryRecordingMetadataEncoder();
                    binaryMetadataEncoder.wrap(wrappedBuffer, limit + headerLength);
                    recordingMetadata.serialize(binaryMetadataEncoder);
                    int typeSerializedLength = binaryMetadataEncoder.encodedLength();
                    encoder.limit(limit + headerLength + typeSerializedLength);
                    wrappedBuffer.putInt(limit, typeSerializedLength, java.nio.ByteOrder.LITTLE_ENDIAN);
                }
        );
        writer.append(binaryList);
    }

    @Override
    public synchronized void write(TypeList types) {
        if (types.getRawBytes().isEmpty()) {
            return;
        }
        writer.append(types.getRawBytes());
    }

    @Override
    public synchronized void write(RecordedMethodCallList callRecords) {
        if (callRecords.getRawBytes().isEmpty()) {
            return;
        }
        writer.append(callRecords.getRawBytes());
    }

    @Override
    public synchronized void write(MethodList methods) {
        if (methods.getRawBytes().isEmpty()) {
            return;
        }
        writer.append(methods.getRawBytes());
    }

    @Override
    public synchronized void close() {
        writer.close();
    }
}
