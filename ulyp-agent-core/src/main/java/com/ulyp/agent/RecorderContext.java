package com.ulyp.agent;

import com.ulyp.agent.options.AgentOptions;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.PrintingRecorder;
import com.ulyp.core.recorders.arrays.ByteArrayRecorder;
import com.ulyp.core.recorders.arrays.ObjectArrayRecorder;
import com.ulyp.core.recorders.collections.CollectionRecorder;
import com.ulyp.core.recorders.collections.MapRecorder;

public class RecorderContext {

    private final AgentOptions options;

    public RecorderContext(AgentOptions options) {
        this.options = options;
    }

    public void init() {
        CollectionRecorder recorder = (CollectionRecorder) ObjectRecorderRegistry.COLLECTION_RECORDER.getInstance();
        recorder.setMode(options.getCollectionsRecordingMode().get());

        MapRecorder mapRecorder = (MapRecorder) ObjectRecorderRegistry.MAP_RECORDER.getInstance();
        mapRecorder.setMode(options.getCollectionsRecordingMode().get());

        PrintingRecorder toStringRecorder = (PrintingRecorder) (ObjectRecorderRegistry.TO_STRING_RECORDER.getInstance());
        toStringRecorder.addTypeMatchers(options.getTypesToPrint().get());

        ByteArrayRecorder byteArrayRecorder = (ByteArrayRecorder) ObjectRecorderRegistry.BYTE_ARRAY_RECORDER.getInstance();
        ObjectArrayRecorder objectArrayRecorder = (ObjectArrayRecorder) ObjectRecorderRegistry.OBJECT_ARRAY_RECORDER.getInstance();
        if (options.getArraysRecordingOption().get()) {
            objectArrayRecorder.setEnabled(true);
            byteArrayRecorder.setEnabled(true);
        }
        objectArrayRecorder.setMaxItemsToRecord(options.getMaxItemsArrayRecordingOption().get());
    }
}
