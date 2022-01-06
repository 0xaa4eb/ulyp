package com.ulyp.storage.impl;

import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.mem.MethodCallList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.process.ProcessInfo;
import com.ulyp.storage.StorageWriter;

import java.io.File;
import java.io.IOException;

public class StorageWriterImpl implements StorageWriter {

    private final BinaryListFileWriter writer;

    public StorageWriterImpl(File file) throws IOException {
        this.writer = new BinaryListFileWriter(file);
    }

    @Override
    public void store(ProcessInfo processInfo) throws IOException {
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
    public void store(TypeList types) throws IOException {
        writer.append(types.getRawBytes());
    }

    @Override
    public void store(MethodCallList callRecords) throws IOException {
        writer.append(callRecords.getRawBytes());
    }

    @Override
    public void store(MethodList methods) throws IOException {
        writer.append(methods.getRawBytes());
    }
}
