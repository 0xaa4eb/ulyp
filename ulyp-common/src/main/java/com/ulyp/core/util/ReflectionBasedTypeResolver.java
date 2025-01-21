package com.ulyp.core.util;

import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ReflectionBasedTypeResolver implements TypeResolver {

    //    private static final TypeResolver INSTANCE = new ReflectionBasedTypeResolver2(2 * 1024 * 1024);
    private static final TypeResolver INSTANCE = new ReflectionBasedTypeResolver();

    public static TypeResolver getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<Class<?>, Type> map = new ConcurrentHashMap<>();
    private final AtomicInteger idGen = new AtomicInteger();
    private final ConcurrentArrayList<Type> allResolved = new ConcurrentArrayList<>();

    private Type build(Class<?> clazz) {
        Type.TypeBuilder type = Type.builder();
        type.name(clazz.getName());
        type.id(idGen.incrementAndGet());
        return type.build();
    }

    @Override
    public @NotNull Type get(Object o) {
        if (o != null) {
            return get(o.getClass());
        } else {
            return Type.unknown();
        }
    }

    @Override
    public @NotNull Type get(Class<?> clazz) {
        Type type = map.get(clazz);
        if (type != null) {
            return type;
        }

        return map.computeIfAbsent(
                clazz,
                klass -> {
                    Type newType = build(clazz);
                    allResolved.add(newType);
                    return newType;
                }
        );
    }

    @Override
    public @NotNull ConcurrentArrayList<Type> values() {
        return allResolved;
    }
}
