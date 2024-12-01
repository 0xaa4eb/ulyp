package com.ulyp.agent;

import lombok.extern.slf4j.Slf4j;

/**
 * Passes all objects to the background thread by reference. Can only be enabled if collections and arrays recording
 * is turned off.
 */
@Slf4j
public class PassByRefRecordedObjectConverter implements RecordedObjectConverter {

    public static final RecordedObjectConverter INSTANCE = new PassByRefRecordedObjectConverter();

    public Object prepare(Object arg) {
        return arg;
    }

    public Object[] prepare(Object[] args) {
        return args;
    }
}
