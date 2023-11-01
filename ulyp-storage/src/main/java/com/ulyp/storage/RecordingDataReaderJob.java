package com.ulyp.storage;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;

public interface RecordingDataReaderJob extends AutoCloseable {

    void onProcessMetadata(ProcessMetadata processMetadata);

    void onRecordingMetadata(RecordingMetadata recordingMetadata);

    void onTypes(TypeList types);

    void onMethods(MethodList methods);

    /**
     *
     */
    void onRecordedCalls(long address, RecordedMethodCallList recordedMethodCalls);

    boolean continueOnNoData();

    void onComplete();
}
