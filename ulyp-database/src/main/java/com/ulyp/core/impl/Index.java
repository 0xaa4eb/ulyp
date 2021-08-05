package com.ulyp.core.impl;

import com.ulyp.database.DatabaseException;
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

    long getSubtreeCount(long callId) throws DatabaseException;

    void setSubtreeCount(long callId, long count) throws DatabaseException;

    long getEnterCallAddress(long callId) throws DatabaseException;

    long getExitCallAddress(long callId) throws DatabaseException;

    void setChildren(long callId, LongList children) throws DatabaseException;

    LongList getChildren(long callId) throws DatabaseException;

    void updateEnterCallAddress(long callId, long address) throws DatabaseException;

    void updateExitCallAddress(long callId, long address) throws DatabaseException;
}
