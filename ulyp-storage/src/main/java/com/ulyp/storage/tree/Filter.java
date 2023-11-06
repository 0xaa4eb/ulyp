package com.ulyp.storage.tree;

public interface Filter {

    static Filter defaultFilter() {
        return recording -> true;
    }

    boolean shouldPublish(Recording recording);
}
