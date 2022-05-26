package com.ulyp.agent.util;

import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.TypeTrait;
import com.ulyp.core.util.ClassUtils;
import com.ulyp.core.util.LoggingSettings;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Converts byte buddy type description to internal domain class {@link Type}
 */
@Slf4j
public class ByteBuddyTypeResolver implements TypeResolver {

    private static final TypeDescription.Generic BYTE_BUDDY_STRING_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(String.class);

    private static final Set<TypeDescription.Generic> PRIMITIVE_INTEGRAL_TYPES = new HashSet<>();
    private static final Set<TypeDescription.Generic> BOXED_INTEGRAL_TYPES = new HashSet<>();
    private static final Set<TypeDescription.Generic> PRIMITIVE_DOUBLE_TYPES = new HashSet<>();
    private static final TypeDescription.Generic PRIMITIVE_CHAR_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(char.class);
    private static final TypeDescription.Generic BOXED_CHAR_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Character.class);
    private static final TypeDescription CLASS_OBJECT_ERASED = TypeDescription.Generic.CLASS.asErasure();
    private static final TypeDescription COLLECTION_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Collection.class).asErasure();
    private static final TypeDescription MAP_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Map.class).asErasure();
    private static final TypeDescription THROWABLE_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Throwable.class).asErasure();
    private static final TypeDescription.Generic PRIMITIVE_BOOLEAN = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(boolean.class);
    private static final TypeDescription.Generic BOXED_BOOLEAN = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Boolean.class);
    private static final AtomicLong typeIdGenerator = new AtomicLong(0L);

    static {
        PRIMITIVE_INTEGRAL_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(long.class));
        PRIMITIVE_INTEGRAL_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(int.class));
        PRIMITIVE_INTEGRAL_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(short.class));
        PRIMITIVE_INTEGRAL_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(byte.class));

        PRIMITIVE_DOUBLE_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(double.class));
        PRIMITIVE_DOUBLE_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(float.class));

        BOXED_INTEGRAL_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Long.class));
        BOXED_INTEGRAL_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Integer.class));
        BOXED_INTEGRAL_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Short.class));
        BOXED_INTEGRAL_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Byte.class));
    }

    private final Map<Class<?>, Type> types = new ConcurrentHashMap<>();

    public static ByteBuddyTypeResolver getInstance() {
        return InstanceHolder.context;
    }

    @NotNull
    @Override
    public Type get(Object object) {
        Type resolvedType;

        if (object != null) {
            resolvedType = types.computeIfAbsent(
                    object.getClass(),
                    this::resolve
            );
        } else {
            resolvedType = Type.unknown();
        }

        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Resolved object of java class {} to type {}", (object != null ? object.getClass() : null), resolvedType);
        }

        return resolvedType;
    }

    @NotNull
    @Override
    public Type get(Class<?> clazz) {
        Type resolvedType;

        if (clazz != null) {
            resolvedType = types.computeIfAbsent(
                    clazz,
                    this::resolve
            );
        } else {
            resolvedType = Type.unknown();
        }

        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Resolved object of java class {} to type {}", clazz, resolvedType);
        }

        return resolvedType;
    }

    private Type resolve(Class<?> clazz) {
        return resolve(TypeDescription.ForLoadedType.of(clazz).asGenericType());
    }

    public Type resolve(TypeDescription.Generic type) {
        try {
            Set<String> superTypes = getSuperTypes(type);
            Set<TypeTrait> typeTraits = deriveTraits(type, superTypes);

            return Type.builder()
                    .id(typeIdGenerator.incrementAndGet())
                    .name(trimGenerics(type.getActualName()))
                    .superTypeNames(superTypes)
                    .superTypeSimpleNames(superTypes.stream().map(ClassUtils::getSimpleNameFromName).collect(Collectors.toSet()))
                    .typeTraits(typeTraits)
                    .build();
        } catch (Throwable ex) {
            return Type.unknown();
        }
    }

    private Set<TypeTrait> deriveTraits(TypeDescription.Generic type, Set<String> superTypes) {
        Set<TypeTrait> traits = EnumSet.noneOf(TypeTrait.class);
        TypeDefinition.Sort sort = type.getSort();

        if (sort == TypeDefinition.Sort.VARIABLE || sort == TypeDefinition.Sort.VARIABLE_SYMBOLIC || sort == TypeDefinition.Sort.WILDCARD) {
            traits.add(TypeTrait.TYPE_VAR);
        } else if (type.isInterface()) {
            traits.add(TypeTrait.INTERFACE);
        } else if (!type.isAbstract()) {
            traits.add(TypeTrait.CONCRETE_CLASS);
        }

        if (type.equals(TypeDescription.Generic.OBJECT)) {
            traits.add(TypeTrait.JAVA_LANG_OBJECT);
        } else if (type.equals(BYTE_BUDDY_STRING_TYPE)) {
            traits.add(TypeTrait.JAVA_LANG_STRING);
        } else if (type.isArray()) {
            if (type.getComponentType().isPrimitive()) {
                traits.add(TypeTrait.PRIMITIVE_ARRAY);
            } else {
                traits.add(TypeTrait.NON_PRIMITIVE_ARRAY);
            }
        } else if (type.isPrimitive()) {
            traits.add(TypeTrait.PRIMITIVE);
            if (PRIMITIVE_CHAR_TYPE.equals(type)) {
                traits.add(TypeTrait.CHAR);
            }
            if (PRIMITIVE_INTEGRAL_TYPES.contains(type)) {
                traits.add(TypeTrait.INTEGRAL);
            }
            if (PRIMITIVE_DOUBLE_TYPES.contains(type)) {
                traits.add(TypeTrait.FRACTIONAL);
            }
            if (PRIMITIVE_BOOLEAN.equals(type)) {
                traits.add(TypeTrait.BOOLEAN);
            }
        } else if (superTypes.contains("java.lang.Throwable") || type.asErasure().equals(THROWABLE_TYPE)) {
            traits.add(TypeTrait.THROWABLE);
        } else if (superTypes.contains("java.lang.Number")) {
            traits.add(TypeTrait.NUMBER);
            if (BOXED_INTEGRAL_TYPES.contains(type)) {
                traits.add(TypeTrait.INTEGRAL);
            }
        } else if (BOXED_CHAR_TYPE.equals(type)) {
            traits.add(TypeTrait.CHAR);
        } else if (BOXED_BOOLEAN.equals(type)) {
            traits.add(TypeTrait.BOOLEAN);
        } else if (type.isEnum()) {
            traits.add(TypeTrait.ENUM);
        } else if (superTypes.contains("java.util.Collection") || type.asErasure().equals(COLLECTION_TYPE)) {
            traits.add(TypeTrait.COLLECTION);
        } else if (superTypes.contains("java.util.Map") || type.asErasure().equals(MAP_TYPE)) {
            traits.add(TypeTrait.MAP);
        } else if (type.asErasure().equals(CLASS_OBJECT_ERASED)) {
            traits.add(TypeTrait.CLASS_OBJECT);
        }

        return traits;
    }

    private Set<String> getSuperTypes(TypeDescription.Generic type) {
        Set<String> superTypes = new HashSet<>();
        try {
            TypeDefinition.Sort sort = type.getSort();
            if (sort != TypeDefinition.Sort.VARIABLE && sort != TypeDefinition.Sort.VARIABLE_SYMBOLIC && sort != TypeDefinition.Sort.WILDCARD) {
                while (type != null && !type.equals(TypeDescription.Generic.OBJECT)) {

                    String actualName = type.asErasure().getActualName();
                    if (actualName.contains("$")) {
                        actualName = actualName.replace('$', '.');
                    }

                    superTypes.add(actualName);

                    for (TypeDescription.Generic interfface : type.getInterfaces()) {
                        addInterfaceAndAllParentInterfaces(superTypes, interfface);
                    }

                    type = type.getSuperClass();
                }
            }
        } catch (Exception e) {
            // NOP
        }
        return superTypes;
    }

    private void addInterfaceAndAllParentInterfaces(Set<String> superTypes, TypeDescription.Generic interfface) {
        superTypes.add(prepareTypeName(interfface.asErasure().getActualName()));

        for (TypeDescription.Generic parentInterface : interfface.getInterfaces()) {
            addInterfaceAndAllParentInterfaces(superTypes, parentInterface);
        }
    }

    private String trimGenerics(String genericName) {
        int pos = genericName.indexOf('<');
        if (pos > 0) {
            genericName = genericName.substring(0, pos);
        }
        return genericName;
    }

    private String prepareTypeName(String genericName) {
        genericName = trimGenerics(genericName);

        if (genericName.contains("$")) {
            genericName = genericName.replace('$', '.');
        }
        return genericName;
    }

    @NotNull
    @Override
    public Collection<Type> getAllResolved() {
        return types.values();
    }

    private static class InstanceHolder {
        private static final ByteBuddyTypeResolver context = new ByteBuddyTypeResolver();
    }
}
