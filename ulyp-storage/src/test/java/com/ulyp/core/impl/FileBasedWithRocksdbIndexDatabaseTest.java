package com.ulyp.core.impl;

import com.ulyp.core.CallRecordDatabase;
import com.ulyp.core.MethodInfoDatabase;
import com.ulyp.core.TypeInfoDatabase;
import com.ulyp.storage.StorageException;

public class FileBasedWithRocksdbIndexDatabaseTest extends CallRecordDatabaseTest {

    @Override
    protected CallRecordDatabase build(MethodInfoDatabase methodInfoDatabase, TypeInfoDatabase typeInfoDatabase) throws StorageException {
        return new LegacyFileBasedCallRecordDatabase(FileBasedWithRocksdbIndexDatabaseTest.class.getSimpleName(), methodInfoDatabase, typeInfoDatabase, new RocksdbIndex());
    }
}