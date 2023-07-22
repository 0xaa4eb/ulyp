package com.ulyp.agent.util;

import com.ulyp.core.Type;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class ByteBuddyTypeConverter {

    public static final ByteBuddyTypeConverter INSTANCE = new ByteBuddyTypeConverter(false);
    public static final ByteBuddyTypeConverter SUPER_TYPE_DERIVING_INSTANCE = new ByteBuddyTypeConverter(true);

    private static final AtomicInteger typeIdGenerator = new AtomicInteger(0);

    private final boolean deriveSuperTypes;

    public ByteBuddyTypeConverter(boolean deriveSuperTypes) {
        this.deriveSuperTypes = deriveSuperTypes;
    }

    public Type convert(TypeDescription.Generic type) {
        try {

            Type.TypeBuilder typeBuilder = Type.builder()
                .id(typeIdGenerator.incrementAndGet())
                .name(trimGenerics(type.getActualName()));

            if (deriveSuperTypes) {
                typeBuilder.superTypeNames(deriveSuperTypes(type));
            }

            return typeBuilder.build();
        } catch (Throwable ex) {
            return Type.unknown();
        }
    }

    private Set<String> deriveSuperTypes(TypeDescription.Generic type) {
        Set<String> superTypes = new HashSet<>();
        try {

            TypeDefinition.Sort sort = type.getSort();
            TypeDescription.Generic superTypeToCheck = type;

            if (sort != TypeDefinition.Sort.VARIABLE && sort != TypeDefinition.Sort.VARIABLE_SYMBOLIC && sort != TypeDefinition.Sort.WILDCARD) {
                while (superTypeToCheck != null && !superTypeToCheck.equals(TypeDescription.Generic.OBJECT)) {

                    // do not add type name to super types
                    if (type != superTypeToCheck) {

                        String actualName = superTypeToCheck.asErasure().getActualName();
                        if (actualName.contains("$")) {
                            actualName = actualName.replace('$', '.');
                        }

                        superTypes.add(actualName);
                    }

                    for (TypeDescription.Generic interfface : superTypeToCheck.getInterfaces()) {
                        addInterfaceAndAllParentInterfaces(superTypes, interfface);
                    }

                    superTypeToCheck = superTypeToCheck.getSuperClass();
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
}
