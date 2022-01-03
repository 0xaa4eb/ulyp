package com.ulyp.storage;

import com.ulyp.core.mem.MethodCallList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;

public interface StorageWriter {

    void store(TypeList types);

    void store(MethodCallList callRecords);

    void store(MethodList methods);
}
