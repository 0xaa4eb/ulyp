package com.ulyp.core.bytes;

public interface Borrowable {

    /**
     * Returns borrowed memory to the pool for further use
     */
    void dispose();
}
