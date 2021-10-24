package com.ulyp.core;

import com.ulyp.core.util.ConcurrentArrayBasedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Comment
 */
public class MethodDescriptionMap {

    private static final MethodDescriptionMap INSTANCE = new MethodDescriptionMap();

    public static MethodDescriptionMap getInstance() {
        return INSTANCE;
    }

    private final ConcurrentArrayBasedMap<Method> continueRecordingMethods = new ConcurrentArrayBasedMap<>(64_000);
    private final ConcurrentArrayBasedMap<Method> startRecordingMethods = new ConcurrentArrayBasedMap<>(64_000);

    private MethodDescriptionMap() {
        // Do not use 0 index, so that it's possible to tell if method goes to "start recording"
        // or "continue recording only" bucket
        continueRecordingMethods.put(null);
        startRecordingMethods.put(null);
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
            return -startRecordingMethods.put(method);
        } else {
            return continueRecordingMethods.put(method);
        }
    }

    public Collection<Method> values() {
        Collection<Method> values = new ArrayList<>();
        for (int i = 1; i < startRecordingMethods.length(); i++) {
            Method method = startRecordingMethods.get(i);
            if (method != null) {
                values.add(method);
            } else {
                break;
            }
        }
        for (int i = 1; i < continueRecordingMethods.length(); i++) {
            Method method = continueRecordingMethods.get(i);
            if (method != null) {
                values.add(method);
            } else {
                break;
            }
        }

        return values;
    }
}
