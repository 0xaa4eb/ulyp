package com.ulyp.core.recorders;

import com.ulyp.core.Type;

public class FileRecord extends ObjectRecord {

    private final String path;

    public FileRecord(Type type, String path) {
        super(type);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path;
    }
}
