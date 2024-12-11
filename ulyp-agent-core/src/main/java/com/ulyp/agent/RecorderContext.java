package com.ulyp.agent;

import com.ulyp.agent.options.AgentOptions;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.PrintingRecorder;
import com.ulyp.core.recorders.arrays.ByteArrayRecorder;
import com.ulyp.core.recorders.arrays.CharArrayRecorder;
import com.ulyp.core.recorders.arrays.IntArrayRecorder;
import com.ulyp.core.recorders.arrays.ObjectArrayRecorder;
import com.ulyp.core.recorders.collections.*;

public class RecorderContext {

    private final AgentOptions options;

    public RecorderContext(AgentOptions options) {
        this.options = options;
    }

    public void init() {
        configureCollectionRecorders();
        configurePrintingRecorder();
        configureArrayRecorders();
    }

    private void configurePrintingRecorder() {
        PrintingRecorder toStringRecorder = (PrintingRecorder) (ObjectRecorderRegistry.TO_STRING_RECORDER.getInstance());
        toStringRecorder.addTypeMatchers(options.getTypesToPrint().get());
    }

    private void configureArrayRecorders() {
        ByteArrayRecorder byteArrayRecorder = (ByteArrayRecorder) ObjectRecorderRegistry.BYTE_ARRAY_RECORDER.getInstance();
        CharArrayRecorder charArrayRecorder = (CharArrayRecorder) ObjectRecorderRegistry.CHAR_ARRAY_RECORDER.getInstance();
        ObjectArrayRecorder objectArrayRecorder = (ObjectArrayRecorder) ObjectRecorderRegistry.OBJECT_ARRAY_RECORDER.getInstance();
        IntArrayRecorder intArrayRecorder = (IntArrayRecorder) ObjectRecorderRegistry.INT_ARRAY_RECORDER.getInstance();
        if (options.getArraysRecordingOption().get()) {
            objectArrayRecorder.setEnabled(true);
            byteArrayRecorder.setEnabled(true);
            intArrayRecorder.setEnabled(true);
            charArrayRecorder.setEnabled(true);
        }
        objectArrayRecorder.setMaxItemsToRecord(options.getMaxItemsArrayRecordingOption().get());
        intArrayRecorder.setMaxItemsToRecord(options.getMaxItemsArrayRecordingOption().get());
    }

    private void configureCollectionRecorders() {
        CollectionRecorder recorder = (CollectionRecorder) ObjectRecorderRegistry.COLLECTION_RECORDER.getInstance();
        recorder.setModes(options.getCollectionsRecordingMode().get());
        recorder.setMaxElementsToRecord(options.getMaxItemsCollectionsRecordingOption().get());

        ListRecorder listRecorder = (ListRecorder)  ObjectRecorderRegistry.LIST_RECORDER.getInstance();
        listRecorder.setModes(options.getCollectionsRecordingMode().get());
        listRecorder.setMaxElementsToRecord(options.getMaxItemsCollectionsRecordingOption().get());

        SetRecorder setRecorder = (SetRecorder)  ObjectRecorderRegistry.SET_RECORDER.getInstance();
        setRecorder.setModes(options.getCollectionsRecordingMode().get());
        setRecorder.setMaxElementsToRecord(options.getMaxItemsCollectionsRecordingOption().get());

        QueueRecorder queueRecorder = (QueueRecorder)  ObjectRecorderRegistry.QUEUE_RECORDER.getInstance();
        queueRecorder.setModes(options.getCollectionsRecordingMode().get());
        queueRecorder.setMaxElementsToRecord(options.getMaxItemsCollectionsRecordingOption().get());

        MapRecorder mapRecorder = (MapRecorder) ObjectRecorderRegistry.MAP_RECORDER.getInstance();
        mapRecorder.setModes(options.getCollectionsRecordingMode().get());
        mapRecorder.setMaxEntriesToRecord(options.getMaxItemsCollectionsRecordingOption().get());
    }
}
