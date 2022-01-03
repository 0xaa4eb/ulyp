package com.ulyp.database.flat;

import com.ulyp.core.mem.MethodCallList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.database.DatabaseWriter;
import com.ulyp.storage.impl.ByAddressFileWriter;
import com.ulyp.storage.impl.WithAddressOutputStream;

import java.io.File;
import java.io.IOException;

public class FlatDatabaseWriter implements DatabaseWriter {

    public static final long MAGIC_CONSTANT = Long.MAX_VALUE / 2L;
    public static final long METHODS_ID = 100;
    public static final long TYPES_ID = 101;

    private final WithAddressOutputStream outputStream;
    private final ByAddressFileWriter byAddressFileWriter;

    public FlatDatabaseWriter(File file) throws IOException {
        outputStream = new WithAddressOutputStream(file);
        byAddressFileWriter = new ByAddressFileWriter(file);

        outputStream.writeLong(MAGIC_CONSTANT);
    }

    @Override
    public void writeMethods(MethodList methods) {

    }

    @Override
    public void writeTypes(TypeList types) {

    }

    @Override
    public void writeCalls(MethodCallList calls) {

    }
}
