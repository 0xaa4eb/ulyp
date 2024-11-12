package com.ulyp.core;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface ByIdTypeResolver {

    @NotNull
    Type getType(int id);
}
