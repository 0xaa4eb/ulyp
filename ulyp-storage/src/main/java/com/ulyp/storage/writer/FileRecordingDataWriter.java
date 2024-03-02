package com.ulyp.storage.writer;

import com.ulyp.core.ProcessMetadata;

import com.ulyp.core.RecordingCompleteMark;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.*;
import com.ulyp.core.bytes.BufferBytesOut;
import com.ulyp.core.serializers.ProcessMetadataSerializer;
import com.ulyp.core.serializers.RecordingMetadataSerializer;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.util.BinaryListFileWriter;
import lombok.extern.slf4j.Slf4j;
import org.agrona.ExpandableDirectByteBuffer;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
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
    public void sync(Duration duration) {

    }

    @Override
    public synchronized void write(ProcessMetadata processMetadata) {
        if (fileWriter == null) {
            this.processMetadata = processMetadata;
            return;
        }
        write(writer -> {
            OutputBytesList bytes = new OutputBytesList(ProcessMetadata.WIRE_ID, new BufferBytesOut(new ExpandableDirectByteBuffer()));
            try {
                bytes.add(out -> ProcessMetadataSerializer.instance.serialize(out, processMetadata));
                writer.write(bytes);
                if (LoggingSettings.DEBUG_ENABLED) {
                    log.debug("Has written {} to storage", processMetadata);
                }
            } finally {
                bytes.dispose();
            }
        });
    }

    @Override
    public synchronized void write(RecordingMetadata recordingMetadata) {
        write(writer -> {
            OutputBytesList bytesOut = new OutputBytesList(RecordingMetadata.WIRE_ID, new BufferBytesOut(new ExpandableDirectByteBuffer()));
            bytesOut.add(out -> RecordingMetadataSerializer.instance.serialize(out, recordingMetadata));
            writer.write(bytesOut);
            if (LoggingSettings.DEBUG_ENABLED) {
                log.debug("Has written {} to storage", processMetadata);
            }
        });
    }

    @Override
    public synchronized void write(SerializedTypeList types) {
        if (types.size() == 0) {
            return;
        }
        OutputBytesList bytes = types.getBytes();
        try {
            write(writer -> writer.write(bytes));
        } finally {
            bytes.dispose();
        }
    }

    @Override
    public synchronized void write(SerializedRecordedMethodCallList callRecords) {
        OutputBytesList bytes = callRecords.toBytes();
        try {
            if (bytes.isEmpty()) {
                return;
            }
            write(writer -> {
                writer.write(bytes);
            /*if (LoggingSettings.DEBUG_ENABLED) {
                log.debug("Has written {} recorded calls, {} bytes", callsBytes.size(), callsBytes.byteLength());
            }*/
            });
        } finally {
            bytes.dispose();
        }
    }

    @Override
    public long estimateBytesWritten() {
        return 0;
    }

    @Override
    public synchronized void write(SerializedMethodList methods) {
        if (methods.size() == 0) {
            return;
        }
        OutputBytesList bytes = methods.getBytes();
        try {
            write(writer -> writer.write(bytes));
        } finally {
            bytes.dispose();
        }
    }

    @Override
    public String toString() {
        return "FileStorageWriter";
    }

    @Override
    public synchronized void close() {
        if (fileWriter != null) {
            fileWriter.write(new OutputBytesList(RecordingCompleteMark.WIRE_ID, new BufferBytesOut(new ExpandableDirectByteBuffer())));
            fileWriter.close();
            fileWriter = null;
        }
    }
}
