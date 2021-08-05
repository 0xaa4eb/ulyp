package com.ulyp.core;

import com.ulyp.transport.TMethodInfoDecoder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Iterator;

public class MethodInfoDatabase {

    private final Long2ObjectMap<TMethodInfoDecoder> methodDescriptionMap = new Long2ObjectOpenHashMap<>();

    public synchronized TMethodInfoDecoder find(long id) {
        return methodDescriptionMap.get(id);
    }

    public synchronized void addAll(MethodInfoList methodInfoList) {
        Iterator<TMethodInfoDecoder> iterator = methodInfoList.copyingIterator();
        while (iterator.hasNext()) {
            TMethodInfoDecoder methodDescription = iterator.next();
            methodDescriptionMap.put(methodDescription.id(), methodDescription);
        }
    }


}
