package com.ulyp.storage;

import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.process.ProcessInfo;

import java.io.IOException;

public interface StorageWriter {

    // TODO move to sbe
    void store(ProcessInfo processInfo) throws IOException;

    void store(TypeList types) throws IOException;

    void store(RecordedMethodCallList callRecords) throws IOException;

    void store(MethodList methods) throws IOException;
}
