package com.ulyp.storage;

import com.ulyp.core.mem.MethodCallList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;

import java.io.IOException;

public interface StorageWriter {

    void store(TypeList types) throws IOException;

    void store(MethodCallList callRecords) throws IOException;

    void store(MethodList methods) throws IOException;
}
