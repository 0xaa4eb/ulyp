package com.ulyp.core;

import com.ulyp.core.util.ConcurrentArrayList;

import java.util.ArrayList;
import java.util.Collection;

public class MethodRepository {

    private final ConcurrentArrayList<Method> methods = new ConcurrentArrayList<>(64_000);
    private final ConcurrentArrayList<Method> recordingStartMethods = new ConcurrentArrayList<>(64_000);

    public Method get(int id) {
        return methods.get(id);
    }

    public int putAndGetId(Method method) {
        return methods.add(method);
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
