package com.ulyp.agent;

import com.ulyp.agent.options.AgentOptions;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.PrintingRecorder;
import com.ulyp.core.recorders.arrays.ObjectArrayRecorder;
import com.ulyp.core.recorders.collections.CollectionRecorder;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
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

        ObjectArrayRecorder objectArrayRecorder = (ObjectArrayRecorder) ObjectRecorderRegistry.OBJECT_ARRAY_RECORDER.getInstance();
        /*
        * If at least Java collections are recorded, we disable array recorder either
        */
        if (options.getCollectionsRecordingMode().get() != CollectionsRecordingMode.NONE) {
            objectArrayRecorder.setEnabled(true);
        }
    }
}
