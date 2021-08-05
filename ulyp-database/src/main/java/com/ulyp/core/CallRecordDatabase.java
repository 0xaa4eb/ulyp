package com.ulyp.core;

import com.ulyp.database.DatabaseException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Database which manages a particular call record tree.
 */
public interface CallRecordDatabase {

    void persistBatch(CallEnterRecordList enterRecords, CallExitRecordList exitRecords) throws DatabaseException;

    /**
    * @return the root of the call record tree. The root node stands for the first method which is called in a
    * recording session.
    */
    CallRecord getRoot() throws DatabaseException;

    CallRecord find(long id) throws DatabaseException;

    default List<CallRecord> getChildren(long id) throws DatabaseException {
        return getChildrenIds(id).stream().map(callId -> {
            try {
                return this.find(callId);
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    List<Long> getChildrenIds(long id) throws DatabaseException;

    /**
     * @return total count of call records in the tree
     */
    long countAll();

    long getSubtreeCount(long id) throws DatabaseException;

    void close();
}
