package com.ulyp.core.util;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@TestOnly
public class ReflectionBasedTypeResolver implements TypeResolver, ByIdTypeResolver {

    private final ConcurrentMap<Class<?>, Type> map = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Type> byIdIndex = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong();

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
        return map.computeIfAbsent(
                clazz,
                klass -> {
                    Type type = Type.builder().name(klass.getName()).id(idGen.incrementAndGet()).build();
                    byIdIndex.put(type.getId(), type);
                    return type;
                }
        );
    }

    @Override
    public @NotNull Collection<Type> getAllResolved() {
        return map.values();
    }

    @Override
    public @NotNull ConcurrentArrayList<Type> getAllResolvedAsConcurrentList() {
        return null;
    }

    @Override
    public @NotNull Type getType(long id) {
        Type type = byIdIndex.get(id);
        if (type != null) {
            return type;
        } else {
            return Type.unknown();
        }
    }
}
