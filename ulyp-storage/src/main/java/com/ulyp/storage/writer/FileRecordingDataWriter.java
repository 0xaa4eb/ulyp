package com.ulyp.storage.writer;

import com.ulyp.core.ProcessMetadata;

import com.ulyp.core.RecordingCompleteMark;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.util.BinaryListFileWriter;
import com.ulyp.transport.BinaryProcessMetadataEncoder;
import com.ulyp.transport.BinaryRecordingMetadataEncoder;
import lombok.extern.slf4j.Slf4j;
import org.agrona.MutableDirectBuffer;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
public class FileRecordingDataWriter implements RecordingDataWriter {

    private final File file;
    private ProcessMetadata processMetadata;
    private BinaryListFileWriter fileWriter;

    public FileRecordingDataWriter(File file) throws StorageException {
        this.file = file;
    }

    private void write(Consumer<BinaryListFileWriter> writer) {
        if (fileWriter == null) {
            try {
                this.fileWriter = new BinaryListFileWriter(file);
            } catch (IOException e) {
                throw new StorageException("Could not build storage for file " + file, e);
            }
            if (processMetadata != null) {
                write(processMetadata);
            }
        }
        writer.accept(fileWriter);
    }

    @Override
    public void reset(ResetRequest resetRequest) throws StorageException {
        write(BinaryListFileWriter::moveToBeginning);
        write(resetRequest.getProcessMetadata());
        write(resetRequest.getTypes());
        write(resetRequest.getMethods());
    }

    @Override
    public synchronized void write(ProcessMetadata processMetadata) {
        if (fileWriter == null) {
            this.processMetadata = processMetadata;
            return;
        }
        write(writer -> {
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
            if (LoggingSettings.DEBUG_ENABLED) {
                log.debug("Has written {} to storage", processMetadata);
            }
        });
    }

    @Override
    public synchronized void write(RecordingMetadata recordingMetadata) {
        write(writer -> {
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
            if (LoggingSettings.DEBUG_ENABLED) {
                log.debug("Has written {} to storage", recordingMetadata);
            }
        });
    }

    @Override
    public synchronized void write(TypeList types) {
        if (types.getRawBytes().isEmpty()) {
            return;
        }
        write(writer -> writer.append(types.getRawBytes()));
    }

    @Override
    public synchronized void write(RecordedMethodCallList callRecords) {
        BinaryList callsBytes = callRecords.getRawBytes();
        if (callsBytes.isEmpty()) {
            return;
        }
        write(writer -> {
            writer.append(callsBytes);
            if (LoggingSettings.DEBUG_ENABLED) {
                log.debug("Has written {} recorded calls, {} bytes", callsBytes.size(), callsBytes.byteLength());
            }
        });
    }

    @Override
    public synchronized void write(MethodList methods) {
        if (methods.getRawBytes().isEmpty()) {
            return;
        }
        write(writer -> writer.append(methods.getRawBytes()));
    }

    @Override
    public String toString() {
        return "FileStorageWriter";
    }

    @Override
    public synchronized void close() {
        if (fileWriter != null) {
            fileWriter.append(new BinaryList(RecordingCompleteMark.WIRE_ID));
            fileWriter.close();
        }
    }
}
