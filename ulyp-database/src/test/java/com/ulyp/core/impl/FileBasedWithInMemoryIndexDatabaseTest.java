package com.ulyp.core.impl;

import com.ulyp.core.CallRecordDatabase;
import com.ulyp.core.MethodInfoDatabase;
import com.ulyp.core.TypeInfoDatabase;
import com.ulyp.database.DatabaseException;

public class FileBasedWithInMemoryIndexDatabaseTest extends CallRecordDatabaseTest {

    @Override
    protected CallRecordDatabase build(MethodInfoDatabase methodInfoDatabase, TypeInfoDatabase typeInfoDatabase) throws DatabaseException {
        return new FileBasedCallRecordDatabase(FileBasedWithInMemoryIndexDatabaseTest.class.getSimpleName(), methodInfoDatabase, typeInfoDatabase, new InMemoryIndex());
    }
}