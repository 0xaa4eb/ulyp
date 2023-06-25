package com.ulyp.core;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.RecorderChooser;
import com.ulyp.transport.BinaryTypeDecoder;
import com.ulyp.transport.BinaryTypeEncoder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

/**
 * Domain class for all java types. All type-related logic uses this class. It is also
 * written to the output recording file and later is read in UI
 */
@Builder
@AllArgsConstructor
@ToString(exclude = {"suggestedRecorder"})
public class Type {

    private static final Type UNKNOWN = Type.builder().name("Unknown").id(-1).build();

    private final int id;
    private final String name;
    @Builder.Default
    private final Set<TypeTrait> typeTraits = EnumSet.noneOf(TypeTrait.class);
    @Builder.Default
    private final Set<String> superTypeNames = Collections.emptySet();

    private volatile ObjectRecorder suggestedRecorder;

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

    public void serialize(BinaryTypeEncoder encoder) {
        encoder.id(this.id);
        encoder.name(this.name);
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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<String> getSuperTypeNames() {
        return superTypeNames;
    }
}
