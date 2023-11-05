package com.ulyp.storage.util;

import com.ulyp.core.util.FileUtil;
import com.ulyp.storage.tree.Index;
import com.ulyp.storage.tree.RecordedCallState;
import com.ulyp.storage.tree.RocksdbIndex;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class RocksdbChecker {

    public static RocksdbAvailableResult checkRocksdbAvailable() {
        Path tempDirectory;
        try {
            tempDirectory = Files.createTempDirectory("ulyp.RocksdbChecker.checkRocksdbAvailable");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (Index index = new RocksdbIndex(tempDirectory)) {

            LongList childrenCallIds = new LongArrayList();
            childrenCallIds.add(32L);
            childrenCallIds.add(62L);

            long callId = 555L;

            RecordedCallState value = RecordedCallState.builder()
                    .id(callId)
                    .childrenCallIds(childrenCallIds)
                    .build();

            index.store(callId, value);

            RecordedCallState valueRead = index.get(callId);

            if (valueRead.getId() != value.getId()) {
                throw new IllegalArgumentException("Inconsistency value read");
            }
            return new RocksdbAvailableResult(true);
        } catch (Throwable err) {
            return new RocksdbAvailableResult(false, err);
        } finally {
            try {
                FileUtil.deleteDirectory(tempDirectory);
            } catch (IOException e) {
                log.warn("Failed to delete temp directory {}", tempDirectory, e);
            }
        }
    }
}
