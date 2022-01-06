package com.ulyp.core.impl;

import com.ulyp.core.CallRecordDatabase;
import com.ulyp.core.MethodInfoDatabase;
import com.ulyp.core.TypeInfoDatabase;
import com.ulyp.storage.StoreException;

public class FileBasedWithInMemoryIndexDatabaseTest extends CallRecordDatabaseTest {

    @Override
    protected CallRecordDatabase build(MethodInfoDatabase methodInfoDatabase, TypeInfoDatabase typeInfoDatabase) throws StoreException {
        return new LegacyFileBasedCallRecordDatabase(FileBasedWithInMemoryIndexDatabaseTest.class.getSimpleName(), methodInfoDatabase, typeInfoDatabase, new InMemoryIndex());
    }
}