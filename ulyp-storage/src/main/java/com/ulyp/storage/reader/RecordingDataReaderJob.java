package com.ulyp.storage.reader;

import com.ulyp.core.Method;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;

public interface RecordingDataReaderJob {

    void onProcessMetadata(ProcessMetadata processMetadata);

    void onRecordingMetadata(RecordingMetadata recordingMetadata);

    void onType(Type type);

    void onMethod(Method method);

    void onRecordedCalls(long address, RecordedMethodCallList recordedMethodCalls);

    boolean continueOnNoData();
}
