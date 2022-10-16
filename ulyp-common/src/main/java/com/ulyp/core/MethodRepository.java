package com.ulyp.core;

import com.ulyp.core.util.ConcurrentArrayList;

import java.util.ArrayList;
import java.util.Collection;


public class MethodRepository {

    public static final int RECORD_METHODS_MIN_ID = 1_000_000_000;

    private static final MethodRepository INSTANCE = new MethodRepository();

    private final ConcurrentArrayList<Method> methods = new ConcurrentArrayList<>(64_000);
    private final ConcurrentArrayList<Method> recordingStartMethods = new ConcurrentArrayList<>(64_000);

    private MethodRepository() {
    }

    public static MethodRepository getInstance() {
        return INSTANCE;
    }

    public Method get(int id) {
        if (id < RECORD_METHODS_MIN_ID) {
            return methods.get(id);
        } else {
            return recordingStartMethods.get(id - RECORD_METHODS_MIN_ID);
        }
    }

    public int putAndGetId(Method method) {
        if (method.shouldStartRecording()) {
            return RECORD_METHODS_MIN_ID + recordingStartMethods.add(method);
        } else {
            return methods.add(method);
        }
    }

    public ConcurrentArrayList<Method> getMethods() {
        return methods;
    }

    public ConcurrentArrayList<Method> getRecordingStartMethods() {
        return recordingStartMethods;
    }

    public Collection<Method> values() {
        Collection<Method> values = new ArrayList<>();

        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);
            if (method != null) {
                values.add(method);
            }
        }

        for (int i = 0; i < recordingStartMethods.size(); i++) {
            Method method = recordingStartMethods.get(i);
            if (method != null) {
                values.add(method);
            }
        }

        return values;
    }
}
