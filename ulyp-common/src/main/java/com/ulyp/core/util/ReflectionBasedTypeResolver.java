package com.ulyp.core.util;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.TypeTrait;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestOnly
public class ReflectionBasedTypeResolver implements TypeResolver {

    private static final ReflectionBasedTypeResolver INSTANCE = new ReflectionBasedTypeResolver();

    public static ReflectionBasedTypeResolver getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<Class<?>, Type> map = new ConcurrentHashMap<>();
    private final AtomicInteger idGen = new AtomicInteger();
    private final ConcurrentArrayList<Type> typesList = new ConcurrentArrayList<>();

    private Type build(Class<?> clazz) {
        Type.TypeBuilder type = Type.builder();
        type.name(clazz.getName());
        type.id(idGen.incrementAndGet());
        type.typeTraits(deriveTraits(clazz));
        return type.build();
    }

    private Set<TypeTrait> deriveTraits(Class<?> clazz) {
        Set<TypeTrait> traits = EnumSet.noneOf(TypeTrait.class);

        if (clazz.isInterface()) {
            traits.add(TypeTrait.INTERFACE);
        } else if (!Modifier.isAbstract(clazz.getModifiers())) {
            traits.add(TypeTrait.CONCRETE_CLASS);
        }
        if (clazz == Object.class) {
            traits.add(TypeTrait.JAVA_LANG_OBJECT);
        } else if (clazz == String.class) {
            traits.add(TypeTrait.JAVA_LANG_STRING);
        } else if (clazz.isArray()) {
            Class<?> componentType = clazz.getComponentType();
            if (componentType.isPrimitive()) {
                if (componentType == byte.class) {
                    traits.add(TypeTrait.PRIMITIVE_BYTE_ARRAY);
                }
            } else {
                traits.add(TypeTrait.NON_PRIMITIVE_ARRAY);
            }
        } else if (clazz.isPrimitive()) {
            traits.add(TypeTrait.PRIMITIVE);
            if (clazz == char.class) {
                traits.add(TypeTrait.CHAR);
            }
            if (clazz == int.class || clazz == long.class || clazz == short.class || clazz == byte.class) {
                traits.add(TypeTrait.INTEGRAL);
            } else if (clazz == float.class || clazz == double.class) {
                traits.add(TypeTrait.FRACTIONAL);
            } else if (clazz == boolean.class) {
                traits.add(TypeTrait.BOOLEAN);
            }

        } else if (Throwable.class.isAssignableFrom(clazz)) {
            traits.add(TypeTrait.THROWABLE);
        } else if (Number.class.isAssignableFrom(clazz)) {
            traits.add(TypeTrait.NUMBER);
            if (clazz == Long.class || clazz == Integer.class || clazz == Short.class || clazz == Byte.class) {
                traits.add(TypeTrait.INTEGRAL);
            }
        } else if (clazz == Class.class) {
            traits.add(TypeTrait.CLASS_OBJECT);
        } else if (clazz == Character.class) {
            traits.add(TypeTrait.CHAR);
        } else if (clazz == Boolean.class) {
            traits.add(TypeTrait.BOOLEAN);
        } else if (clazz.isEnum()) {
            traits.add(TypeTrait.ENUM);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            traits.add(TypeTrait.COLLECTION);
        } else if (Map.class.isAssignableFrom(clazz)) {
            traits.add(TypeTrait.MAP);
        }

        return traits;
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
        return map.computeIfAbsent(
                clazz,
                klass -> {
                    Type type = build(clazz);
                    typesList.add(type);
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
        return typesList;
    }
/*    @Override
    public @NotNull Type getType(long id) {
        Type type = byIdIndex.get(id);
        if (type != null) {
            return type;
        } else {
            return Type.unknown();
        }
    }*/
}
