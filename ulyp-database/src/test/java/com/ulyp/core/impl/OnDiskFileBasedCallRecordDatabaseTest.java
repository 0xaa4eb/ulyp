package com.ulyp.core.impl;

import com.ulyp.core.CallRecordDatabase;

public class OnDiskFileBasedCallRecordDatabaseTest extends CallRecordDatabaseTest {

    @Override
    protected CallRecordDatabase build() {
        return new OnDiskFileBasedCallRecordDatabase();
    }
}