package com.ulyp.storage.tree;

import com.ulyp.core.bytes.DirectBytesIn;
import com.ulyp.core.bytes.BufferBytesOut;
import com.ulyp.storage.StorageException;
import com.ulyp.core.util.BitUtil;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteOptions;

import java.nio.file.Path;

public class RocksdbIndex implements Index {

    private final RocksDB db;
    private final WriteOptions writeOptions;
    private final ThreadLocal<MutableDirectBuffer> tempBuffer = ThreadLocal.withInitial(() -> new ExpandableDirectByteBuffer(64 * 1024));
    private final ThreadLocal<byte[]> keyBuffer = ThreadLocal.withInitial(() -> new byte[Long.BYTES]);

    public RocksdbIndex(Path indexFolder) throws StorageException {
        try {
            Options options = new Options();
            options.setCreateIfMissing(true);

            db = RocksDB.open(options, indexFolder.toAbsolutePath().toString());

            writeOptions = new WriteOptions();
            writeOptions.setSync(false);
            writeOptions.setDisableWAL(true);
        } catch (RocksDBException ioException) {
            throw new StorageException("Could not create RocksDB index", ioException);
        }
    }

    @Override
    public CallRecordIndexState get(long id) {
        byte[] keyBytes = keyBuffer.get();
        BitUtil.longToBytes(id, keyBytes, 0);
        byte[] bytes;
        try {
            bytes = db.get(keyBytes);
        } catch (RocksDBException e) {
            throw new StorageException("Could not read", e);
        }
        if (bytes == null) {
            return null;
        }
        return BinaryRecordedCallStateSerializer.instance.deserialize(new DirectBytesIn(bytes));
    }

    @Override
    public void store(long id, CallRecordIndexState value) {
        MutableDirectBuffer buffer = tempBuffer.get();
        BufferBytesOut binaryOutput = new BufferBytesOut(buffer);
        BinaryRecordedCallStateSerializer.instance.serialize(binaryOutput, value);

        byte[] keyBytes = keyBuffer.get();
        BitUtil.longToBytes(id, keyBytes, 0);

        int bytesWritten = binaryOutput.position();
        byte[] valueBytes = new byte[bytesWritten];
        buffer.getBytes(0, valueBytes);
        try {
            db.put(writeOptions, keyBytes, 0, keyBytes.length, valueBytes, 0, bytesWritten);
        } catch (RocksDBException e) {
            throw new StorageException("Could not write", e);
        }
    }

    @Override
    public void close() throws RuntimeException {
        db.close();
    }
}
