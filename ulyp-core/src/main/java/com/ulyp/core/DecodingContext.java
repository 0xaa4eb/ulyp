package com.ulyp.core;

import com.ulyp.core.printers.TypeInfo;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

public class DecodingContext implements ByIdTypeResolver {

    private final Long2ObjectMap<TypeInfo> classIdMap;

    public DecodingContext(Long2ObjectMap<TypeInfo> classIdMap) {
        this.classIdMap = classIdMap;
    }

    public TypeInfo getType(long id) {
        return classIdMap.get(id);
    }
}
