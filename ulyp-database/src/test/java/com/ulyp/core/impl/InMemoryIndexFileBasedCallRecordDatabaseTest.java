package com.ulyp.core.impl;

import com.ulyp.core.CallRecordDatabase;

public class InMemoryIndexFileBasedCallRecordDatabaseTest extends CallRecordDatabaseTest {

    @Override
    protected CallRecordDatabase build() {
        return new InMemoryIndexFileBasedCallRecordDatabase();
    }
}