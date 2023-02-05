package com.ulyp.storage;

public interface Filter {

    static Filter defaultFilter() {
        return recording -> true;
    }

    boolean shouldPublish(Recording recording);
}
