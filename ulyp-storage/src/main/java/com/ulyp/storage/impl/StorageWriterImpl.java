package com.ulyp.storage.impl;

import com.ulyp.core.mem.MethodCallList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.StorageWriter;

import java.io.File;
import java.io.IOException;

public class StorageWriterImpl implements StorageWriter {

    private final BinaryListFileWriter binaryListFileWriter;

    public StorageWriterImpl(File file) throws IOException {
        this.binaryListFileWriter = new BinaryListFileWriter(file);
    }

    @Override
    public void store(TypeList types) throws IOException {
        binaryListFileWriter.append(types.getRawBytes());
    }

    @Override
    public void store(MethodCallList callRecords) throws IOException {
        binaryListFileWriter.append(callRecords.getRawBytes());
    }

    @Override
    public void store(MethodList methods) throws IOException {
        binaryListFileWriter.append(methods.getRawBytes());
    }
}
