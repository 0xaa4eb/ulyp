package com.ulyp.storage;

@FunctionalInterface
public interface RecordingListener {

    static RecordingListener empty() {
        return recording -> {
        };
    }

    void onRecordingUpdated(Recording recording);
}
