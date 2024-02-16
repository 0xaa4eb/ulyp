package com.ulyp.core;

import com.ulyp.core.util.ConcurrentArrayList;

import java.util.ArrayList;
import java.util.Collection;

public class MethodRepository {

    private final ConcurrentArrayList<Method> methods = new ConcurrentArrayList<>(64_000);

    public Method get(int id) {
        return methods.get(id);
    }

    public int putAndGetId(Method method) {
        // TODO we rely on method.id here which is bad
        int id = methods.add(method);
        if (method.getId() != id) {
            System.out.println(method + " put at id " + id);
        }
        return id;
    }

    public ConcurrentArrayList<Method> getMethods() {
        return methods;
    }

    public Collection<Method> values() {
        Collection<Method> values = new ArrayList<>();
        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);
            if (method != null) {
                values.add(method);
            }
        }
        return values;
    }
}
