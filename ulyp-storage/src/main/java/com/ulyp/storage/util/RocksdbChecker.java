package com.ulyp.storage.util;

import com.ulyp.core.util.FileUtil;
import com.ulyp.storage.tree.CallRecordIndexState;
import com.ulyp.storage.tree.Index;
import com.ulyp.storage.tree.RocksdbIndex;
import lombok.extern.slf4j.Slf4j;
import org.agrona.collections.LongArrayList;

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

            LongArrayList childrenCallIds = new LongArrayList();
            childrenCallIds.add(32L);
            childrenCallIds.add(62L);

            long callId = 555L;

            CallRecordIndexState value = CallRecordIndexState.builder()
                    .id(callId)
                    .childrenCallIds(childrenCallIds)
                    .build();

            index.store(callId, value);

            CallRecordIndexState valueRead = index.get(callId);

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
