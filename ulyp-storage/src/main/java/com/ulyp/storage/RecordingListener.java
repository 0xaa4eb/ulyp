package com.ulyp.storage;

public interface RecordingListener {

    static RecordingListener empty() {
        return recording -> {};
    }

    void onRecordingUpdated(Recording recording);
}
