package com.ulyp.core;

import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.RecorderChooser;
import com.ulyp.transport.BinaryTypeDecoder;
import com.ulyp.transport.BinaryTypeEncoder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Domain class for all java types. All type-related logic uses this class. It is also
 * written to the output recording file and later is read in UI
 */
@Builder
@AllArgsConstructor
@ToString(exclude = {"suggestedRecorder", "writtenToFile"})
public class Type {

    private static final Type UNKNOWN = Type.builder().name("Unknown").id(-1).build();

    private final long id;
    private final String name;
    @Builder.Default
    private final Set<TypeTrait> typeTraits = EnumSet.noneOf(TypeTrait.class);
    @Builder.Default
    private final Set<String> superTypeNames = new HashSet<>();
    @Builder.Default
    private final Set<String> superTypeSimpleNames = new HashSet<>();

    private volatile ObjectRecorder suggestedRecorder;
    // If was dumped to the output file
    @Builder.Default
    private volatile boolean writtenToFile = false;

    public static Type unknown() {
        return UNKNOWN;
    }

    public static Type deserialize(BinaryTypeDecoder decoder) {
        String name = decoder.name();

        return Type.builder()
                .id(decoder.id())
                .name(name)
                .build();
    }

    public ObjectRecorder getSuggestedRecorder() {
        ObjectRecorder recorder = suggestedRecorder;
        if (recorder != null) {
            return recorder;
        } else {
            return suggestedRecorder = RecorderChooser.getInstance().chooseForType(this);
        }
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

    public void serialize(BinaryTypeEncoder encoder) {
        encoder.id(this.id);
        encoder.name(this.name);
    }
}
