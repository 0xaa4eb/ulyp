package com.ulyp.storage;

@FunctionalInterface
public interface Filter {

    boolean accept(Recording recording);
}
