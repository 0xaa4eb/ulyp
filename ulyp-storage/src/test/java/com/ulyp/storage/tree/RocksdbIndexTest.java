package com.ulyp.storage.tree;

import java.io.IOException;
import java.nio.file.Files;

public class RocksdbIndexTest extends IndexTest {
    @Override
    protected Index buildIndex() throws IOException {
        return new RocksdbIndex(Files.createTempDirectory("RocksdbIndexTest"));
    }
}
