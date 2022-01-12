package com.ulyp.core.impl;

import com.ulyp.storage.StorageException;
import it.unimi.dsi.fastutil.longs.LongList;

public interface Index {

    default void update(CallRecordIndexMetadata metadata) throws StorageException {
        updateEnterCallAddress(metadata.getCallId(), metadata.getEnterAddressPos());
        if (metadata.getExitAddressPos() > 0) {
            updateExitCallAddress(metadata.getCallId(), metadata.getExitAddressPos());
        }
        setSubtreeCount(metadata.getCallId(), metadata.getSubtreeCount());
        setChildren(metadata.getCallId(), metadata.getChildren());
    }

    long getSubtreeCount(long callId) throws StorageException;

    void setSubtreeCount(long callId, long count) throws StorageException;

    long getEnterCallAddress(long callId) throws StorageException;

    long getExitCallAddress(long callId) throws StorageException;

    void setChildren(long callId, LongList children) throws StorageException;

    LongList getChildren(long callId) throws StorageException;

    void updateEnterCallAddress(long callId, long address) throws StorageException;

    void updateExitCallAddress(long callId, long address) throws StorageException;
}
