package com.ulyp.core.log;

import java.util.Date;

public class SysOutLogger implements Logger {

    @Override
    public void error(String msg, Throwable e) {
        if (LoggingSettings.ERROR_ENABLED) {
            synchronized (System.out) {
                System.out.println("ULYP " + new Date() + " | " + Thread.currentThread().getName() + " | ERROR | " + msg + ", exception msg: " + e.getMessage());
//                e.printStackTrace();
            }
        }
    }

    @Override
    public void info(String msg) {
        if (LoggingSettings.INFO_ENABLED) {
            System.out.println("ULYP " + new Date() + " | " + Thread.currentThread().getName() + " | INFO | " + msg);
        }
    }

    @Override
    public void debug(String msg) {
        if (LoggingSettings.DEBUG_ENABLED) {
            System.out.println("ULYP " + new Date() + " | " + Thread.currentThread().getName() + " | DEBUG | " + msg);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return LoggingSettings.DEBUG_ENABLED;
    }
}
