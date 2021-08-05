package com.ulyp.core.impl;

import com.ulyp.database.DatabaseException;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.*;

@SuppressWarnings("unused")
public class InMemoryIndex implements Index {

    private static final LongArrayList EMPTY_LIST = new LongArrayList();

    private final Long2ObjectMap<LongArrayList> children = new Long2ObjectOpenHashMap<>();
    private final Long2LongMap idToSubtreeCountMap = new Long2LongOpenHashMap();
    private final Long2LongMap enterRecordPos = new Long2LongOpenHashMap();
    private final Long2LongMap exitRecordPos = new Long2LongOpenHashMap();

    public InMemoryIndex() {
        exitRecordPos.defaultReturnValue(-1L);
        enterRecordPos.defaultReturnValue(-1L);
        idToSubtreeCountMap.defaultReturnValue(0);
    }

    @Override
    public long getSubtreeCount(long callId) {
        return idToSubtreeCountMap.get(callId);
    }

    @Override
    public void setSubtreeCount(long callId, long count) throws DatabaseException {
        idToSubtreeCountMap.put(callId, count);
    }

    @Override
    public long getEnterCallAddress(long callId) {
        return enterRecordPos.get(callId);
    }

    @Override
    public long getExitCallAddress(long callId) {
        return exitRecordPos.get(callId);
    }

    @Override
    public void setChildren(long callId, LongList children) throws DatabaseException {
        if (!children.isEmpty()) {
            this.children.put(callId, new LongArrayList(children));
        }
    }

    @Override
    public LongList getChildren(long callId) {
        return children.getOrDefault(callId, EMPTY_LIST);
    }

    @Override
    public void updateEnterCallAddress(long callId, long address) {
        enterRecordPos.put(callId, address);
    }

    @Override
    public void updateExitCallAddress(long callId, long address) {
        exitRecordPos.put(callId, address);
    }
}
