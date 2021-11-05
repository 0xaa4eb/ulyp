package com.ulyp.core;

import com.ulyp.core.printers.ObjectRecorder;
import com.ulyp.core.printers.Printers;
import com.ulyp.transport.BinaryTypeDecoder;
import com.ulyp.transport.BinaryTypeEncoder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@ToString
public class Type {

    private static final Type UNKNOWN = Type.builder().name("Unknown").id(-1).build();

    private final long id;
    private final String name;
    @Builder.Default
    private final Set<TypeTrait> typeTraits = EnumSet.noneOf(TypeTrait.class);
    @Builder.Default
    private final boolean hasToStringMethod = false;
    @Builder.Default
    private final Set<String> superTypeNames = new HashSet<>();
    @Builder.Default
    private final Set<String> superTypeSimpleNames = new HashSet<>();

    private volatile ObjectRecorder suggestedPrinter;
    // If was dumped to the output file
    @Builder.Default
    private volatile boolean writtenToFile = false;

    public ObjectRecorder getSuggestedPrinter() {
        ObjectRecorder printer = suggestedPrinter;
        if (printer != null) {
            return printer;
        } else {
            return suggestedPrinter = Printers.getInstance().determinePrinterForReturnType(this);
        }
    }

    public static Type unknown() {
        return UNKNOWN;
    }

    public Set<TypeTrait> getTraits() {
        return typeTraits;
    }

    public boolean isExactlyJavaLangObject() {
        return typeTraits.contains(TypeTrait.JAVA_LANG_OBJECT);
    }

    public boolean isExactlyJavaLangString() {
        return typeTraits.contains(TypeTrait.JAVA_LANG_STRING);
    }

    public boolean isNonPrimitveArray() {
        return typeTraits.contains(TypeTrait.NON_PRIMITIVE_ARRAY);
    }

    public boolean wasWrittenToFile() {
        return writtenToFile;
    }

    public void setWrittenToFile() {
        writtenToFile = true;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<String> getSuperTypeNames() {
        return superTypeNames;
    }

    public Set<String> getSuperTypeSimpleNames() {
        return superTypeSimpleNames;
    }

    public boolean isEnum() {
        return typeTraits.contains(TypeTrait.ENUM);
    }

    public boolean isInterface() {
        return typeTraits.contains(TypeTrait.INTERFACE);
    }

    public boolean isTypeVar() {
        return typeTraits.contains(TypeTrait.TYPE_VAR);
    }

    public boolean isCollection() {
        return typeTraits.contains(TypeTrait.COLLECTION);
    }

    public boolean isClassObject() {
        return typeTraits.contains(TypeTrait.CLASS_OBJECT);
    }

    public boolean hasToStringMethod() {
        return hasToStringMethod;
    }

    public void serialize(BinaryTypeEncoder encoder) {
        encoder.id(this.id);
        BinaryTypeEncoder.SuperTypeNamesEncoder superTypeNamesEncoder = encoder.superTypeNamesCount(superTypeNames.size());
        for (String val : superTypeNames) {
            superTypeNamesEncoder.next().value(val);
        }
        BinaryTypeEncoder.SuperTypeSimpleNamesEncoder superTypeSimpleNamesEncoder = encoder.superTypeSimpleNamesCount(superTypeSimpleNames.size());
        for (String val : superTypeSimpleNames) {
            superTypeSimpleNamesEncoder.next().value(val);
        }
        encoder.name(this.name);
    }

    public static Type deserialize(BinaryTypeDecoder decoder) {
        Set<String> superTypeNames = new HashSet<>();
        decoder.superTypeNames().forEachRemaining(val -> superTypeNames.add(val.value()));

        Set<String> superTypeSimpleNames = new HashSet<>();
        decoder.superTypeSimpleNames().forEachRemaining(val -> superTypeSimpleNames.add(val.value()));

        String name = decoder.name();

        return Type.builder()
                .id(decoder.id())
                .name(name)
                .superTypeNames(superTypeNames)
                .superTypeSimpleNames(superTypeSimpleNames)
                .build();
    }
}
