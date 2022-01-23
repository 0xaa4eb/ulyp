package com.ulyp.ui

import com.ulyp.core.*
import com.ulyp.core.impl.LegacyFileBasedCallRecordDatabase
import com.ulyp.core.impl.RocksdbIndex
import com.ulyp.core.recorders.ObjectRecorderType
import com.ulyp.core.util.SingleTypeReflectionBasedResolver
import com.ulyp.transport.TClassDescription
import lombok.Value
import java.util.concurrent.atomic.AtomicLong

class ByThreadIdAggregationStrategy : AggregationStrategy {

    private val idGen = AtomicLong(0L)

    override fun getId(chunk: CallRecordTreeChunk): CallRecordTreeTabId {
        return Key(chunk.recordingInfo.threadId, chunk.recordingInfo.threadName)
    }

    override fun buildDatabase(
        methodInfoDatabase: MethodInfoDatabase?,
        typeInfoDatabase: TypeInfoDatabase?
    ): CallRecordDatabase {
        val typeResolver: TypeResolver = SingleTypeReflectionBasedResolver(Int.MAX_VALUE.toLong(), Thread::class.java)
        typeInfoDatabase!!.addAll(
            listOf(
                TClassDescription.newBuilder().setId(Int.MAX_VALUE.toLong()).setName(
                    Thread::class.java.name
                ).build()
            )
        )
        val methodInfos = MethodInfoList()
        val threadRunMethod = Method.builder()
            .id(Int.MAX_VALUE.toLong())
            .name("run")
            .returnsSomething(false)
            .isStatic(false)
            .isConstructor(false)
            .declaringType(typeResolver[Thread::class.java])
            .build()
        methodInfos.add(threadRunMethod)
        methodInfoDatabase!!.addAll(methodInfos)
        val database: CallRecordDatabase = LegacyFileBasedCallRecordDatabase(
                "" + idGen.incrementAndGet(),
                methodInfoDatabase,
                typeInfoDatabase,
                RocksdbIndex()
        )
        val enterRecords = CallEnterRecordList()
        val exitRecords = CallExitRecordList()
        enterRecords.add(
            0, Int.MAX_VALUE.toLong(),
            typeResolver, arrayOf(ObjectRecorderType.IDENTITY_RECORDER.instance),
            Thread.currentThread(), arrayOf()
        )
        database.persistBatch(enterRecords, exitRecords)
        return database
    }

    @Value
    private class Key(val threadId: Long, var threadName: String) : CallRecordTreeTabId {
        // Aggregate by <thread id, thread name> because same id (as well as name) might be reused by same JVM

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Key

            if (threadId != other.threadId) return false
            if (threadName != other.threadName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = threadId.hashCode()
            result = 31 * result + threadName.hashCode()
            return result
        }
    }
}