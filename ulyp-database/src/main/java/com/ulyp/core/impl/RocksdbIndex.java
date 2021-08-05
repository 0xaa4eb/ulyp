package com.ulyp.core.impl;

import com.ulyp.database.DatabaseException;
import com.ulyp.database.util.ByteUtils;
import it.unimi.dsi.fastutil.longs.*;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class RocksdbIndex implements Index {

    private static final LongList EMPTY_LIST = new LongArrayList();

    private final RocksDB db;

    public RocksdbIndex() throws DatabaseException {
        try {
            Path ulyp = Files.createTempDirectory("ulyp");

            Options options = new Options();
            options.setCreateIfMissing(true);

            db = RocksDB.open(options, ulyp.toAbsolutePath().toString());
        } catch (RocksDBException | IOException ioException) {
            throw new DatabaseException(ioException);
        }
    }

    @Override
    public long getSubtreeCount(long callId) throws DatabaseException {
        try {
            byte[] bytes = db.get(("c:" + callId).getBytes(StandardCharsets.UTF_8));
            if (bytes != null) {
                return ByteUtils.bytesToLong(bytes);
            } else {
                return 0;
            }
        } catch (RocksDBException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void setSubtreeCount(long callId, long count) throws DatabaseException {
        try {
            db.put(("c:" + callId).getBytes(StandardCharsets.UTF_8), ByteUtils.longToBytes(count));
        } catch (RocksDBException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public long getEnterCallAddress(long callId) throws DatabaseException {
        try {
            byte[] bytes = db.get(("e:" + callId).getBytes(StandardCharsets.UTF_8));
            if (bytes == null) {
                return -1L;
            } else {
                return ByteUtils.bytesToLong(bytes);
            }
        } catch (RocksDBException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public long getExitCallAddress(long callId) throws DatabaseException {
        try {
            byte[] bytes = db.get(("z:" + callId).getBytes(StandardCharsets.UTF_8));
            if (bytes == null) {
                return -1L;
            } else {
                return ByteUtils.bytesToLong(bytes);
            }
        } catch (RocksDBException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void setChildren(long callId, LongList children) throws DatabaseException {
        try {
            db.put(
                    ("ch:" + callId).getBytes(StandardCharsets.UTF_8),
                    ByteUtils.longsToBytes(children)
            );
        } catch (RocksDBException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public LongList getChildren(long callId) throws DatabaseException {
        try {
            byte[] bytes = db.get(("ch:" + callId).getBytes(StandardCharsets.UTF_8));
            if (bytes == null) {
                return new LongArrayList();
            } else {
                return ByteUtils.bytesToLongs(bytes);
            }
        } catch (RocksDBException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void updateEnterCallAddress(long callId, long address) throws DatabaseException {
        try {
            db.put(("e:" + callId).getBytes(StandardCharsets.UTF_8), ByteUtils.longToBytes(address));
        } catch (RocksDBException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void updateExitCallAddress(long callId, long address) throws DatabaseException {
        try {
            db.put(("z:" + callId).getBytes(StandardCharsets.UTF_8), ByteUtils.longToBytes(address));
        } catch (RocksDBException e) {
            throw new DatabaseException(e);
        }
    }
}
