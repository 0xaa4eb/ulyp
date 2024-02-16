package com.ulyp.core;

import com.ulyp.core.recorders.ObjectRecorder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

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

    private final int id;
    private final String name;
    @Builder.Default
    private final Set<String> superTypeNames = Collections.emptySet();

    private volatile ObjectRecorder recorderHint;

    public static Type unknown() {
        return UNKNOWN;
    }

    public ObjectRecorder getRecorderHint() {
        return recorderHint;
    }

    public void setRecorderHint(ObjectRecorder recorderHint) {
        this.recorderHint = recorderHint;
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
