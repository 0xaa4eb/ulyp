package com.ulyp.agent;

import com.ulyp.agent.api.RecordingEnabled;
import com.ulyp.agent.api.ResetRecordingFileRequest;

/**
* Exposed for direct call from instrumented apps
*/
public class Api {

    public static void resetRecordingFile() {
        ApiHolder.instance.resetRecordingFile(
                ResetRecordingFileRequest.newBuilder().build(),
                new NoopStreamObserver<>()
        );
    }

    public static void startRecording() {
        ApiHolder.instance.setRecording(
                RecordingEnabled.newBuilder().setValue(true).getDefaultInstanceForType(),
                new NoopStreamObserver<>()
        );
    }

    public static void endRecording() {
        ApiHolder.instance.setRecording(
                RecordingEnabled.newBuilder().setValue(false).getDefaultInstanceForType(),
                new NoopStreamObserver<>()
        );
    }
}
