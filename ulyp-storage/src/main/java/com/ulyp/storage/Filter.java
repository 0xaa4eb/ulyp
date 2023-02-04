package com.ulyp.storage;

public interface Filter {

    boolean shouldPublish(Recording recording);
}
