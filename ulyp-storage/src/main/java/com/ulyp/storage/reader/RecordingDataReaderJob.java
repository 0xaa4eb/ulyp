package com.ulyp.storage.reader;

import com.ulyp.core.Method;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;

public interface RecordingDataReaderJob {

    default void onStart() {}

    void onProcessMetadata(ProcessMetadata processMetadata);

    void onRecordingMetadata(RecordingMetadata recordingMetadata);

    void onType(Type type);

    void onMethod(Method method);

    void onRecordedCalls(long address, RecordedMethodCalls recordedMethodCalls);

    boolean continueOnNoData();
}
