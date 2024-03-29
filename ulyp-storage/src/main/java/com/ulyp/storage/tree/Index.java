package com.ulyp.storage.tree;

/**
 * Index is used to store record call states tree. Every state usually includes enter and exit record calls addresses
 * (address is relative position in the recording file), children ids and subtree size.
 * The tree may have millions of calls, so the primary index implementation must be disk based.
 */
public interface Index extends AutoCloseable {

    CallRecordIndexState get(long id);

    void store(long id, CallRecordIndexState callState);

    void close() throws RuntimeException;
}
