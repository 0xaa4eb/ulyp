package com.ulyp.agent.util;

import com.ulyp.core.printers.*;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ByteBuddyTypeInfo implements TypeInfo {

    private static final TypeDescription.Generic BYTE_BUDDY_STRING_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(String.class);

    private static final Set<TypeDescription.Generic> PRIMITIVE_INTEGRAL_TYPES = new HashSet<>();
    private static final Set<TypeDescription.Generic> BOXED_INTEGRAL_TYPES = new HashSet<>();
    private static final Set<TypeDescription.Generic> PRIMITIVE_DOUBLE_TYPES = new HashSet<>();
    private static final TypeDescription CLASS_OBJECT_ERASED = TypeDescription.Generic.CLASS.asErasure();
    private static final TypeDescription COLLECTION_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Collection.class).asErasure();
    private static final TypeDescription MAP_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Map.class).asErasure();
    private static final TypeDescription THROWABLE_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Throwable.class).asErasure();

    private static final AtomicInteger classDescriptionId = new AtomicInteger(0);

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

    private final int id;
    private final String actualName;
    private final Set<TypeTrait> typeTraits;
    private boolean hasToStringMethod;
    private volatile ObjectBinaryPrinter suggestedPrinter;
    private final Set<String> superClassesNames = new HashSet<>();
    private final Set<String> interfacesClassesNames = new HashSet<>();

    public static TypeInfo of(Class<?> clazz) {
        return of(TypeDescription.ForLoadedType.of(clazz).asGenericType());
    }

    public static TypeInfo of(TypeDescription.Generic type) {
        try {
            return new ByteBuddyTypeInfo(type);
        } catch (Throwable e) {
            return UnknownTypeInfo.getInstance();
        }
    }

    private ByteBuddyTypeInfo(TypeDescription.Generic type) {
        // DO NOT store reference to type here. There could be millions of type objects, they consume memory pretty good
        this.id = classDescriptionId.incrementAndGet();
        this.actualName = type.getActualName();
        this.addSuperTypes(type);
        this.typeTraits = derive(type);

        try {
            if (!this.typeTraits.contains(TypeTrait.TYPE_VAR)) {
                hasToStringMethod = type.getDeclaredMethods().stream().anyMatch(
                        method -> method.getActualName().equals("toString") && method.getReturnType().equals(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(String.class))
                );
            } else {
                hasToStringMethod = false;
            }
        } catch (Throwable e) {
            hasToStringMethod = false;
        }
    }

    private Set<TypeTrait> derive(TypeDescription.Generic type) {
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
            if (PRIMITIVE_INTEGRAL_TYPES.contains(type)) {
                traits.add(TypeTrait.INTEGRAL);
            }
            if (PRIMITIVE_DOUBLE_TYPES.contains(type)) {
                traits.add(TypeTrait.FRACTIONAL);
            }
        } else if (getSuperClassesNames().contains("java.lang.Throwable") || type.asErasure().equals(THROWABLE_TYPE)) {
            traits.add(TypeTrait.THROWABLE);
        } else if (getSuperClassesNames().contains("java.lang.Number")) {
            traits.add(TypeTrait.NUMBER);
            if (BOXED_INTEGRAL_TYPES.contains(type)) {
                traits.add(TypeTrait.INTEGRAL);
            }
        } else if (type.isEnum()) {
            traits.add(TypeTrait.ENUM);
        } else if (getInterfacesClassesNames().contains("java.util.Collection") || type.asErasure().equals(COLLECTION_TYPE)) {
            traits.add(TypeTrait.COLLECTION);
        } else if (getInterfacesClassesNames().contains("java.util.Map") || type.asErasure().equals(MAP_TYPE)) {
            traits.add(TypeTrait.MAP);
        } else if (type.asErasure().equals(CLASS_OBJECT_ERASED)) {
            traits.add(TypeTrait.CLASS_OBJECT);
        }

        return traits;
    }

    private void addSuperTypes(TypeDescription.Generic type) {
        try {
            TypeDefinition.Sort sort = type.getSort();
            if (sort != TypeDefinition.Sort.VARIABLE && sort != TypeDefinition.Sort.VARIABLE_SYMBOLIC && sort != TypeDefinition.Sort.WILDCARD) {
                while (type != null && !type.equals(TypeDescription.Generic.OBJECT)) {
                    superClassesNames.add(type.asErasure().getActualName());

                    for (TypeDescription.Generic interfface : type.getInterfaces()) {
                        addInterfaceAndAllParentInterfaces(interfface);
                    }

                    type = type.getSuperClass();
                }
            }
        } catch (Exception e) {
            // NOP
        }
    }

    private void addInterfaceAndAllParentInterfaces(TypeDescription.Generic interfface) {
        interfacesClassesNames.add(trimGenericTypes(interfface.asErasure().getActualName()));

        for (TypeDescription.Generic parentInterface : interfface.getInterfaces()) {
            addInterfaceAndAllParentInterfaces(parentInterface);
        }
    }

    // TODO fix the hack
    private String trimGenericTypes(String genericName) {
        int pos = genericName.indexOf('<');
        if (pos > 0) {
            return genericName.substring(0, pos);
        } else {
            return genericName;
        }
    }

    public ObjectBinaryPrinter getSuggestedPrinter() {
        ObjectBinaryPrinter printer = suggestedPrinter;
        if (printer != null) {
            return printer;
        } else {
            return suggestedPrinter = Printers.getInstance().determinePrinterForReturnType(this);
        }
    }

    @Override
    public Set<TypeTrait> getTraits() {
        return typeTraits;
    }

    @Override
    public boolean isExactlyJavaLangObject() {
        return typeTraits.contains(TypeTrait.JAVA_LANG_OBJECT);
    }

    @Override
    public boolean isExactlyJavaLangString() {
        return typeTraits.contains(TypeTrait.JAVA_LANG_STRING);
    }

    @Override
    public boolean isNonPrimitveArray() {
        return typeTraits.contains(TypeTrait.NON_PRIMITIVE_ARRAY);
    }

    @Override
    public boolean isPrimitiveArray() {
        return typeTraits.contains(TypeTrait.PRIMITIVE_ARRAY);
    }

    @Override
    public boolean isPrimitive() {
        return typeTraits.contains(TypeTrait.PRIMITIVE);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return actualName;
    }

    public Set<String> getSuperClassesNames() {
        return superClassesNames;
    }

    public Set<String> getInterfacesClassesNames() {
        return interfacesClassesNames;
    }

    @Override
    public boolean isEnum() {
        return typeTraits.contains(TypeTrait.ENUM);
    }

    @Override
    public boolean isInterface() {
        return typeTraits.contains(TypeTrait.INTERFACE);
    }

    @Override
    public boolean isTypeVar() {
        return typeTraits.contains(TypeTrait.TYPE_VAR);
    }

    @Override
    public boolean isCollection() {
        return typeTraits.contains(TypeTrait.COLLECTION);
    }

    @Override
    public boolean isClassObject() {
        return typeTraits.contains(TypeTrait.CLASS_OBJECT);
    }

    @Override
    public boolean hasToStringMethod() {
        return hasToStringMethod;
    }

    @Override
    public String toString() {
        return "ByteBuddyType{" +"name=" + getName() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ByteBuddyTypeInfo that = (ByteBuddyTypeInfo) o;

        if (hasToStringMethod != that.hasToStringMethod) return false;
        if (actualName != null ? !actualName.equals(that.actualName) : that.actualName != null) return false;
        if (typeTraits != that.typeTraits) return false;
        if (suggestedPrinter != null ? !suggestedPrinter.equals(that.suggestedPrinter) : that.suggestedPrinter != null)
            return false;
        if (!superClassesNames.equals(that.superClassesNames))
            return false;
        return interfacesClassesNames.equals(that.interfacesClassesNames);
    }

    @Override
    public int hashCode() {
        int result = actualName != null ? actualName.hashCode() : 0;
        result = 31 * result + (typeTraits != null ? typeTraits.hashCode() : 0);
        result = 31 * result + (hasToStringMethod ? 1 : 0);
        result = 31 * result + (suggestedPrinter != null ? suggestedPrinter.hashCode() : 0);
        result = 31 * result + superClassesNames.hashCode();
        result = 31 * result + interfacesClassesNames.hashCode();
        return result;
    }
}
