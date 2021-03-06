package com.ulyp.core;

import com.ulyp.transport.TClassDescription;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Database which manages a particular call record tree.
 */
public interface CallRecordDatabase {

    void persistBatch(
            CallEnterRecordList enterRecords,
            CallExitRecordList exitRecords,
            MethodInfoList methodInfoList,
            List<TClassDescription> classDescriptionList);

    /**
    * @return the root of the call record tree. The root node stands for the first method which is called in a
    * recording session.
    */
    default CallRecord getRoot() {
        return find(0);
    }

    CallRecord find(long id);

    default List<CallRecord> getChildren(long id) {
        return getChildrenIds(id).stream().map(this::find).collect(Collectors.toList());
    }

    List<Long> getChildrenIds(long id);

    /**
     * @return total count of call records in the tree
     */
    long countAll();

    long getSubtreeCount(long id);

    void close();
}
