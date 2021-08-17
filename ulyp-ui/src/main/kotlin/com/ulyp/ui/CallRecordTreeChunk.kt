package com.ulyp.ui

import com.ulyp.transport.ProcessInfo
import com.ulyp.transport.RecordingInfo
import com.ulyp.transport.TCallRecordLogUploadRequest

/**
 * Part of call record tree serialized by the agent
 */
class CallRecordTreeChunk(val request: TCallRecordLogUploadRequest) {

    val processInfo: ProcessInfo
        get() = request.recordingInfo.processInfo

    val recordingInfo: RecordingInfo
        get() = request.recordingInfo

    val recordingId: Long
        get() = request.recordingInfo.recordingId
}