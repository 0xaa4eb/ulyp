package com.ulyp.storage;

import com.ulyp.storage.tree.Recording;

@FunctionalInterface
public interface RecordingListener {

    static RecordingListener empty() {
        return recording -> {
        };
    }

    void onRecordingUpdated(Recording recording);
}
