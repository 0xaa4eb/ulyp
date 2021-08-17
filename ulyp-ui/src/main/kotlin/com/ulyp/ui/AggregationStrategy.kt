package com.ulyp.ui

import com.ulyp.core.CallRecordDatabase
import com.ulyp.core.MethodInfoDatabase
import com.ulyp.core.TypeInfoDatabase

interface AggregationStrategy {

    fun getId(chunk: CallRecordTreeChunk): CallRecordTreeTabId

    fun buildDatabase(methodInfoDatabase: MethodInfoDatabase?, typeInfoDatabase: TypeInfoDatabase?): CallRecordDatabase
}