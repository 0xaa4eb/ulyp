package com.ulyp.storage.tree;

public interface RecordingListener {

    static RecordingListener empty() {
        return new RecordingListener() {
            @Override
            public void onRecordingUpdated(Recording recording) {

            }

            @Override
            public void onProgressUpdated(double progress) {

            }
        };
    }

    void onRecordingUpdated(Recording recording);

    void onProgressUpdated(double progress);
}
