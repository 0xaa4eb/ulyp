package com.ulyp.core;

import com.ulyp.core.printers.TypeInfo;

public interface ByIdTypeResolver {

    TypeInfo getType(long id);
}
