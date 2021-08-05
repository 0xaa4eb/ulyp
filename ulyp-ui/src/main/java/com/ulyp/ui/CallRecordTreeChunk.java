package com.ulyp.ui;

import com.ulyp.core.*;
import com.ulyp.transport.ProcessInfo;
import com.ulyp.transport.RecordingInfo;
import com.ulyp.transport.TCallRecordLogUploadRequest;
import com.ulyp.transport.TCallRecordLogUploadRequestOrBuilder;

/**
 * Part of call record tree serialized by the agent
 */
public class CallRecordTreeChunk {

    private final TCallRecordLogUploadRequest request;

    public CallRecordTreeChunk(TCallRecordLogUploadRequest request) {
        this.request = request;
    }

    public ProcessInfo getProcessInfo() {
        return request.getRecordingInfo().getProcessInfo();
    }

    public RecordingInfo getRecordingInfo() {
        return request.getRecordingInfo();
    }

    public long getRecordingId() {
        return request.getRecordingInfo().getRecordingId();
    }

    public void dispose() {
//        database.deleteSubtree(root.getId());
    }

    public TCallRecordLogUploadRequest getRequest() {
        return request;
    }
}
