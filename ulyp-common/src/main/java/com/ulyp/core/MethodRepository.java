package com.ulyp.core;

import com.ulyp.core.util.ConcurrentArrayList;

import java.util.ArrayList;
import java.util.Collection;


public class MethodRepository {

    private static final MethodRepository INSTANCE = new MethodRepository();

    private final ConcurrentArrayList<Method> methods = new ConcurrentArrayList<>(64_000);

    private MethodRepository() {
        // Do not use 0 index, so that it's possible to tell if method goes to "start recording"
        // or "continue recording only" bucket
        methods.add(null);
    }

    public static MethodRepository getInstance() {
        return INSTANCE;
    }

    public Method get(int id) {
        return methods.get(id);
    }

    public int putAndGetId(Method method) {
        return methods.add(method);
    }

    public Collection<Method> values() {
        Collection<Method> values = new ArrayList<>();

        for (int i = 1; i < methods.size(); i++) {
            Method method = methods.get(i);
            if (method != null) {
                values.add(method);
            }
        }

        return values;
    }
}
