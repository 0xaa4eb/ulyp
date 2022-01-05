package com.ulyp.core.impl;

import com.ulyp.storage.StoreException;
import it.unimi.dsi.fastutil.longs.LongList;

public interface Index {

    default void update(CallRecordIndexMetadata metadata) {
        updateEnterCallAddress(metadata.getCallId(), metadata.getEnterAddressPos());
        if (metadata.getExitAddressPos() > 0) {
            updateExitCallAddress(metadata.getCallId(), metadata.getExitAddressPos());
        }
        setSubtreeCount(metadata.getCallId(), metadata.getSubtreeCount());
        setChildren(metadata.getCallId(), metadata.getChildren());
    }

    long getSubtreeCount(long callId) throws StoreException;

    void setSubtreeCount(long callId, long count) throws StoreException;

    long getEnterCallAddress(long callId) throws StoreException;

    long getExitCallAddress(long callId) throws StoreException;

    void setChildren(long callId, LongList children) throws StoreException;

    LongList getChildren(long callId) throws StoreException;

    void updateEnterCallAddress(long callId, long address) throws StoreException;

    void updateExitCallAddress(long callId, long address) throws StoreException;
}
