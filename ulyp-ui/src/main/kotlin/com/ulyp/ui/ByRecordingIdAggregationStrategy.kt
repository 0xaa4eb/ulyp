package com.ulyp.ui

import com.ulyp.core.CallRecordDatabase
import com.ulyp.core.MethodInfoDatabase
import com.ulyp.core.TypeInfoDatabase
import com.ulyp.core.impl.FileBasedCallRecordDatabase
import com.ulyp.core.impl.RocksdbIndex
import lombok.Value
import java.util.concurrent.atomic.AtomicLong

class ByRecordingIdAggregationStrategy : AggregationStrategy {
    private val idGen = AtomicLong(0L)

    override fun getId(chunk: CallRecordTreeChunk): CallRecordTreeTabId {
        return Key(chunk.recordingId)
    }

    override fun buildDatabase(
        methodInfoDatabase: MethodInfoDatabase?,
        typeInfoDatabase: TypeInfoDatabase?
    ): CallRecordDatabase {
        return FileBasedCallRecordDatabase(
            "" + idGen.incrementAndGet(),
            methodInfoDatabase,
            typeInfoDatabase,
            RocksdbIndex()
        )
    }

    // TODO check lombok support
    @Value
    private class Key(val recordingId: Long) : CallRecordTreeTabId {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Key

            if (recordingId != other.recordingId) return false

            return true
        }

        override fun hashCode(): Int {
            return recordingId.hashCode()
        }
    }
}