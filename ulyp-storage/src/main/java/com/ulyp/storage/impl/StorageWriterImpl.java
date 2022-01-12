package com.ulyp.storage.impl;

import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.process.ProcessInfo;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.StorageWriter;
import com.ulyp.transport.BinaryRecordingMetadataEncoder;
import org.agrona.MutableDirectBuffer;

import java.io.File;
import java.io.IOException;

public class StorageWriterImpl implements StorageWriter {

    private final BinaryListFileWriter writer;

    public StorageWriterImpl(File file) throws IOException {
        this.writer = new BinaryListFileWriter(file);
    }

    @Override
    public void store(ProcessInfo processInfo) {
        BinaryList binaryList = new BinaryList(ProcessInfo.ID);
        binaryList.add(
                com.ulyp.transport.ProcessInfo.newBuilder()
                        .setMainClassName(processInfo.getMainClassName())
                        .addAllClasspath(processInfo.getClasspath().toList())
                        .build()
                        .toByteArray()
        );
        writer.append(binaryList);
    }

    @Override
    public void store(RecordingMetadata recordingMetadata) {
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
    public void store(TypeList types) {
        writer.append(types.getRawBytes());
    }

    @Override
    public void store(RecordedMethodCallList callRecords) {
        writer.append(callRecords.getRawBytes());
    }

    @Override
    public void store(MethodList methods) {
        writer.append(methods.getRawBytes());
    }

    @Override
    public void close() {
        writer.close();
    }
}
