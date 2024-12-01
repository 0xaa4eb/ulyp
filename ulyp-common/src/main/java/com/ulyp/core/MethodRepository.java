package com.ulyp.core;

import com.ulyp.core.util.ConcurrentArrayList;
import lombok.Getter;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Collection;

@Getter
@ThreadSafe
public class MethodRepository {

    private final ConcurrentArrayList<Method> methods = new ConcurrentArrayList<>(64_000);

    public Method get(int id) {
        return methods.get(id);
    }

    public int putAndGetId(Method method) {
        int id = methods.add(method);
        method.setId(id);
        return id;
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
