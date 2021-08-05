package com.ulyp.core;

import com.ulyp.transport.TClassDescription;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.List;

public class TypeInfoDatabase {

    private final Long2ObjectMap<Type> values = new Long2ObjectOpenHashMap<>();

    public synchronized void addAll(List<TClassDescription> classDescriptionList) {
        for (TClassDescription classDescription : classDescriptionList) {
            values.put(
                    classDescription.getId(),
                    Type.builder().name(classDescription.getName()).id(classDescription.getId()).build()
            );
        }
    }

    public synchronized Type find(long id) {
        return values.get(id);
    }
}
