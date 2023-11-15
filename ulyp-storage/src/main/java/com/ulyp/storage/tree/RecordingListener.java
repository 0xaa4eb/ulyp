package com.ulyp.storage.tree;

@FunctionalInterface
public interface RecordingListener {

    static RecordingListener empty() {
        return recording -> {
        };
    }

    void onRecordingUpdated(Recording recording);
}
