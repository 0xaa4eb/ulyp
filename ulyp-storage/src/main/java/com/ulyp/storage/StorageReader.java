package com.ulyp.storage;


import java.util.List;

public interface StorageReader extends AutoCloseable {

    List<Recording> availableRecordings();

    void close() throws StorageException;
}
