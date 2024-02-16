package com.ulyp.core;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@ToString
@EqualsAndHashCode(exclude = "shouldStartRecording")
public class Method {

    private final int id;
    private final String name;
    private final Type declaringType;
    private final boolean isStatic;
    private final boolean isConstructor;
    private final boolean returnsSomething;
    private volatile boolean shouldStartRecording;

    public boolean shouldStartRecording() {
        return shouldStartRecording;
    }

    public void setShouldStartRecording(boolean shouldStartRecording) {
        this.shouldStartRecording = shouldStartRecording;
    }

    public int getId() {
        return id;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public Type getDeclaringType() {
        return declaringType;
    }

    public String getName() {
        return name;
    }

    public boolean returnsSomething() {
        return returnsSomething;
    }

    public String toShortString() {
        return declaringType.getName() + "." + name;
    }
}
