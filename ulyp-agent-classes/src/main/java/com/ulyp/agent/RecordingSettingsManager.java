package com.ulyp.agent;

public class RecordingSettingsManager {

    private static final RecordingSettingsManager instance = new RecordingSettingsManager();

    private volatile int[] methodIds;
}
