package com.ulyp.storage;

import com.ulyp.storage.impl.RecordedCallState;

/**
 * Index is used to store record call states tree. Every state usually includes enter and exit record calls addresses
 * (address is relative position in the recording file), children ids and subtree size.
 * The tree may have millions of calls, so the primary index implementation must be disk based.
 */
public interface Index extends AutoCloseable {

    RecordedCallState get(long id);

    void store(long id, RecordedCallState callState);
}
