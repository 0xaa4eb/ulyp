package com.ulyp.core.log;

public interface Logger {

    void error(String msg, Throwable e);

    void info(String msg);

    void debug(String msg);

    boolean isDebugEnabled();
}
