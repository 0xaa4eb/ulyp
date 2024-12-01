package com.ulyp.agent.options;

@FunctionalInterface
public interface Parser<T> {

    T parse(String text);
}
