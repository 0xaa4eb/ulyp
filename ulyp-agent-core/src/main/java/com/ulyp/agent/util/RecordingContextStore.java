package com.ulyp.agent.util;

import com.ulyp.agent.RecordingThreadLocalContext;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Maintains all recording states {@link RecordingThreadLocalContext} in a simple array for fast access
 * by recording id. The access to array is not synchronized - JVM guarantees everything is fine (no word tearing) as long as
 * different threads access their own exclusive locations.
 */
@NotThreadSafe
public class RecordingContextStore {

    private static final int MAX_RECORDINGS = 64 * 1024;

    private final RecordingThreadLocalContext[] recordingStates = new RecordingThreadLocalContext[MAX_RECORDINGS];
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    public RecordingContextStore() {

    }

    /**
     * Atomically puts recording states and returns an index which might be used as a recording id
     */
    public int add(RecordingThreadLocalContext ctx) {
        int id = generateRecordingId();
        // no synchronization and volatile writes since different thread access different array locations
        recordingStates[id] = ctx;
        return id;
    }

    public RecordingThreadLocalContext get(int recordingId) {
        // no synchronization
        return recordingStates[recordingId];
    }

    public void remove(int recordingId) {
        recordingStates[recordingId] = null;
    }

    private int generateRecordingId() {
        int id = idGenerator.incrementAndGet();
        if (id >= MAX_RECORDINGS) {
            // this is unlikely that 64k recordings will happen, mut maybe support wrapping around recording id in UI
            synchronized (this) {
                // relatively unsafe, but ok
                if (idGenerator.get() >= MAX_RECORDINGS) {
                    idGenerator.set(0);
                    id = idGenerator.incrementAndGet();
                }
            }
        }
        return id;
    }
}
