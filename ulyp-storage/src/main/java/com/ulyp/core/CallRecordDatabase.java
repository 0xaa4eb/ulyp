package com.ulyp.core;

import com.ulyp.storage.StoreException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Database which manages a particular call record tree.
 */
public interface CallRecordDatabase {

    void persistBatch(CallEnterRecordList enterRecords, CallExitRecordList exitRecords) throws StoreException;

    /**
    * @return the root of the call record tree. The root node stands for the first method which is called in a
    * recording session.
    */
    CallRecord getRoot() throws StoreException;

    CallRecord find(long id) throws StoreException;

    default List<CallRecord> getChildren(long id) throws StoreException {
        return getChildrenIds(id).stream().map(callId -> {
            try {
                return this.find(callId);
            } catch (StoreException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    List<Long> getChildrenIds(long id) throws StoreException;

    /**
     * @return total count of call records in the tree
     */
    long countAll();

    long getSubtreeCount(long id) throws StoreException;

    void close();
}
