package com.ulyp.core;

import org.jetbrains.annotations.NotNull;

public interface ByIdTypeResolver {

    @NotNull
    Type getType(long id);
}
