package com.ulyp.core.util;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class SingleTypeReflectionBasedResolver implements TypeResolver, ByIdTypeResolver {

    private final Class<?> javaType;
    private final Type ulypType;

    public SingleTypeReflectionBasedResolver(long id, Class<?> javaType) {
        this.javaType = javaType;
        this.ulypType = Type.builder().name(javaType.getName()).id(id).build();
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
        if (clazz == javaType) {
            return ulypType;
        } else {
            return Type.unknown();
        }
    }

    @Override
    public @NotNull Collection<Type> getAllKnownTypes() {
        return Collections.singletonList(ulypType);
    }

    @Override
    public Type getType(long id) {
        if (id == ulypType.getId()) {
            return ulypType;
        } else {
            return null;
        }
    }
}
