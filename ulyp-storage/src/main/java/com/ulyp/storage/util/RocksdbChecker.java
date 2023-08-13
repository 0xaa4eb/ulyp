package com.ulyp.storage.util;

import com.ulyp.core.util.TempDirectory;
import com.ulyp.storage.impl.RecordedCallState;
import com.ulyp.storage.impl.RocksdbIndex;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

public class RocksdbChecker {

    public static RocksdbAvailableResult checkRocksdbAvailable() {
        RocksdbIndex index = null;
        try {
            TempDirectory tempDirectory = new TempDirectory("ulyp.RocksdbChecker.checkRocksdbAvailable");
            index = new RocksdbIndex(tempDirectory.toPath());

            LongList childrenCallIds = new LongArrayList();
            childrenCallIds.add(32L);
            childrenCallIds.add(62L);

            long callId = 555L;

            RecordedCallState value = RecordedCallState.builder()
                    .callId(callId)
                    .childrenCallIds(childrenCallIds)
                    .build();

            index.store(callId, value);

            RecordedCallState valueRead = index.get(callId);

            if (valueRead.getCallId() != value.getCallId()) {
                throw new IllegalArgumentException("Inconsistency value read");
            }
            return new RocksdbAvailableResult(true);
        } catch (Throwable err) {
            return new RocksdbAvailableResult(false, err);
        }
    }
}
