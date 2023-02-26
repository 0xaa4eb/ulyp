package com.ulyp.core.util;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.TypeTrait;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestOnly
public class ReflectionBasedTypeResolver implements TypeResolver, ByIdTypeResolver {

    private final ConcurrentMap<Class<?>, Type> map = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Type> byIdIndex = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong();

    private Type build(Class<?> clazz) {
        Type.TypeBuilder type = Type.builder();
        type.name(clazz.getName());
        type.id(idGen.incrementAndGet());
        type.typeTraits(deriveTraits(clazz));

        Set<String> superTypes = getSuperTypes(clazz);
        type.superTypeNames(superTypes);
        type.superTypeSimpleNames(superTypes.stream().map(ClassUtils::getSimpleNameFromName).collect(Collectors.toSet()));

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

    private Set<String> getSuperTypes(Class<?> type) {
        Set<String> superTypes = new HashSet<>();
        try {

            Class<?> superTypeToCheck = type;

            while (superTypeToCheck != null && superTypeToCheck != Object.class) {

                // do not add type name to super types
                if (type != superTypeToCheck) {

                    String actualName = superTypeToCheck.getName();
                    if (actualName.contains("$")) {
                        actualName = actualName.replace('$', '.');
                    }

                    superTypes.add(actualName);
                }

                for (Class<?> interfface : superTypeToCheck.getInterfaces()) {
                    addInterfaceAndAllParentInterfaces(superTypes, interfface);
                }

                superTypeToCheck = superTypeToCheck.getSuperclass();
            }
        } catch (Exception e) {
            log.debug("Error while resolving super types for type {}", type, e);
        }
        return superTypes;
    }

    private void addInterfaceAndAllParentInterfaces(Set<String> superTypes, Class<?> interfface) {
        superTypes.add(prepareTypeName(interfface.getName()));

        for (Class<?> parentInterface : interfface.getInterfaces()) {
            addInterfaceAndAllParentInterfaces(superTypes, parentInterface);
        }
    }

    private String prepareTypeName(String genericName) {
        if (genericName.contains("$")) {
            genericName = genericName.replace('$', '.');
        }
        return genericName;
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
                    Type type = Type.builder().name(klass.getName()).id(idGen.incrementAndGet()).build();
                    byIdIndex.put(type.getId(), type);
                    return type;
                }
        );
    }

    @Override
    public @NotNull Collection<Type> getAllResolved() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull ConcurrentArrayList<Type> getAllResolvedAsConcurrentList() {
        return new ConcurrentArrayList<>();
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
