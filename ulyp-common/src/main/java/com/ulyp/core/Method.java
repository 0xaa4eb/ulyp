package com.ulyp.core;

import lombok.*;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class Method {

    @Setter
    private int id;
    private final String name;
    private final Type declaringType;
    private final boolean isStatic;
    private final boolean constructor;
    private final boolean returnsSomething;

    public boolean isStatic() {
        return isStatic;
    }

    public boolean returnsSomething() {
        return returnsSomething;
    }

    public String toShortString() {
        return declaringType.getName() + "." + name;
    }
}
