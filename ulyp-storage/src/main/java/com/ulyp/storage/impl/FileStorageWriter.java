package com.ulyp.storage.impl;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingCompleteMark;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.StorageWriter;
import com.ulyp.transport.BinaryProcessMetadataEncoder;
import com.ulyp.transport.BinaryRecordingMetadataEncoder;
import lombok.extern.slf4j.Slf4j;
import org.agrona.MutableDirectBuffer;

import java.io.File;
import java.io.IOException;

@Slf4j
public class FileStorageWriter implements StorageWriter {

    private final File file;
    private final BinaryListFileWriter fileWriter;

    public FileStorageWriter(File file) throws StorageException {
        try {
            this.file = file;
            this.fileWriter = new BinaryListFileWriter(file);
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
        fileWriter.append(binaryList);
        if (LoggingSettings.INFO_ENABLED) {
            log.info("Has written {} to storage", processMetadata);
        }
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
        fileWriter.append(binaryList);
        if (LoggingSettings.INFO_ENABLED) {
            log.info("Has written {} to storage", recordingMetadata);
        }
    }

    @Override
    public synchronized void write(TypeList types) {
        if (types.getRawBytes().isEmpty()) {
            return;
        }
        fileWriter.append(types.getRawBytes());
    }

    @Override
    public synchronized void write(RecordedMethodCallList callRecords) {
        BinaryList callsBytes = callRecords.getRawBytes();
        if (callsBytes.isEmpty()) {
            return;
        }
        fileWriter.append(callsBytes);
        if (LoggingSettings.INFO_ENABLED) {
            log.info("Has written {} recorded calls, {} bytes", callsBytes.size(), callsBytes.byteLength());
        }
    }

    @Override
    public synchronized void write(MethodList methods) {
        if (methods.getRawBytes().isEmpty()) {
            return;
        }
        fileWriter.append(methods.getRawBytes());
    }

    private synchronized void writePoisonPill() {
        fileWriter.append(new BinaryList(RecordingCompleteMark.WIRE_ID));
    }

    @Override
    public String toString() {
        return "FileStorageWriter";
    }

    @Override
    public synchronized void close() {
        writePoisonPill();
        fileWriter.close();
    }
}
