package com.ulyp.database;

import com.ulyp.core.mem.MethodCallList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;

public interface DatabaseWriter {

    void writeMethods(MethodList methods);

    void writeTypes(TypeList types);

    void writeCalls(MethodCallList calls);
}
