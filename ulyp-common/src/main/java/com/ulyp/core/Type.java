package com.ulyp.core;

import com.ulyp.core.recorders.ObjectRecorder;
import lombok.*;

import java.util.Collections;
import java.util.Set;

/**
 * Domain class for all java types. All type-related logic uses this class. It is also
 * written to the output recording file and later is read in UI
 */
@Builder
@AllArgsConstructor
@EqualsAndHashCode(exclude = "recorderHint")
public class Type {

    private static final Type UNKNOWN = Type.builder().name("Unknown").id(-1).build();
    public static final Type INT = Type.builder().name("int").id(-2).build();

    private final int id;
    private final String name;
    @Builder.Default
    private final Set<String> superTypeNames = Collections.emptySet();
    @Getter
    @Setter
    private volatile ObjectRecorder recorderHint;

    public static Type unknown() {
        return UNKNOWN;
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

    @Override
    public String toString() {
        return "Type{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
