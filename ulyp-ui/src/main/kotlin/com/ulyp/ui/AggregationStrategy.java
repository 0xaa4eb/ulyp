package com.ulyp.ui;

import com.ulyp.core.CallRecordDatabase;
import com.ulyp.core.MethodInfoDatabase;
import com.ulyp.core.TypeInfoDatabase;

public interface AggregationStrategy {

    CallRecordTreeTabId getId(CallRecordTreeChunk chunk);

    CallRecordDatabase buildDatabase(MethodInfoDatabase methodInfoDatabase, TypeInfoDatabase typeInfoDatabase);
}
