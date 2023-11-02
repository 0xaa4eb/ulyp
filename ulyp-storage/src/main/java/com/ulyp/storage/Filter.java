package com.ulyp.storage;

import com.ulyp.storage.tree.Recording;

public interface Filter {

    static Filter defaultFilter() {
        return recording -> true;
    }

    boolean shouldPublish(Recording recording);
}
