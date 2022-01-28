package com.ulyp.core.util;

public interface Backoff {

    void await() throws InterruptedException;
}
