package com.ulyp.core;

import com.ulyp.core.util.ConcurrentArrayList;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Comment
 */
public class MethodDescriptionMap {

    private static final MethodDescriptionMap INSTANCE = new MethodDescriptionMap();

    public static MethodDescriptionMap getInstance() {
        return INSTANCE;
    }

    private final ConcurrentArrayList<Method> continueRecordingMethods = new ConcurrentArrayList<>(64_000);
    private final ConcurrentArrayList<Method> startRecordingMethods = new ConcurrentArrayList<>(64_000);

    private MethodDescriptionMap() {
        // Do not use 0 index, so that it's possible to tell if method goes to "start recording"
        // or "continue recording only" bucket
        continueRecordingMethods.add(null);
        startRecordingMethods.add(null);
    }

    public Method get(int id) {
        if (id > 0) {
            return continueRecordingMethods.get(id);
        } else {
           return startRecordingMethods.get(-id);
        }
    }

    public int putAndGetId(Method method, boolean shouldStartRecording) {
        if (shouldStartRecording) {
            return -startRecordingMethods.add(method);
        } else {
            return continueRecordingMethods.add(method);
        }
    }

    public Collection<Method> values() {
        Collection<Method> values = new ArrayList<>();
        for (int i = 1; i < startRecordingMethods.size(); i++) {
            Method method = startRecordingMethods.get(i);
            if (method != null && !method.wasWrittenToFile()) {
                values.add(method);
            }
        }
        for (int i = 1; i < continueRecordingMethods.size(); i++) {
            Method method = continueRecordingMethods.get(i);
            if (method != null && !method.wasWrittenToFile()) {
                values.add(method);
            }
        }

        return values;
    }
}
