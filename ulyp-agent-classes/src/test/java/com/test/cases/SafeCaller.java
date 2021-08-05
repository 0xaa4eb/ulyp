package com.test.cases;

public class SafeCaller {

    public static void call(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            // NOP
        }
    }
}
