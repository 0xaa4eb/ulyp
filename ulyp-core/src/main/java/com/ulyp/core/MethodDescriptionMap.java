package com.ulyp.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class MethodDescriptionMap {

    private static final MethodDescriptionMap INSTANCE = new MethodDescriptionMap();

    public static MethodDescriptionMap getInstance() {
        return INSTANCE;
    }

    private static final AtomicInteger continueRecordingCounter = new AtomicInteger(0);
    private static final AtomicInteger startOrContinueRecordingCounter = new AtomicInteger(0);

    // TODO make maps
    private final AtomicReferenceArray<MethodInfo> continueRecordingMethods = new AtomicReferenceArray<>(4_000_000);
    private final AtomicReferenceArray<MethodInfo> startRecordingMethods = new AtomicReferenceArray<>(32_000);

    private MethodDescriptionMap() {
    }

    public static boolean shouldStartRecording(long id) {
        return id < 0;
    }

    public MethodInfo get(int id) {
        if (id >= 0) {
            return continueRecordingMethods.get(id);
        } else {
           return startRecordingMethods.get(-id);
        }
    }

    public int putAndGetId(MethodInfo methodInfo, boolean shouldStartRecording) {
        int id;
        if (shouldStartRecording) {
            id = startOrContinueRecordingCounter.decrementAndGet();
            startRecordingMethods.set(-id, methodInfo);
        } else {
            id = continueRecordingCounter.incrementAndGet();
            continueRecordingMethods.set(id, methodInfo);
        }
        return id;
    }

    public Collection<MethodInfo> values() {
        Collection<MethodInfo> values = new ArrayList<>();
        for (int i = 1; i < startRecordingMethods.length(); i++) {
            MethodInfo method = startRecordingMethods.get(i);
            if (method != null) {
                values.add(method);
            } else {
                break;
            }
        }
        for (int i = 1; i < continueRecordingMethods.length(); i++) {
            MethodInfo method = continueRecordingMethods.get(i);
            if (method != null) {
                values.add(method);
            } else {
                break;
            }
        }

        return values;
    }
}
