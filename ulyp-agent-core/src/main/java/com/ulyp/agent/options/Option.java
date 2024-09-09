package com.ulyp.agent.options;

public interface Option<T> {

    T get();

    String getDescription();
}
