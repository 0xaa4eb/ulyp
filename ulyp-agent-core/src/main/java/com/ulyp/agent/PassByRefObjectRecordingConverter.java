package com.ulyp.agent;

import com.ulyp.agent.util.ConstructedTypesStack;
import lombok.extern.slf4j.Slf4j;

/**
 * Passes all objects to the background thread by reference. Can only be enabled if collections and arrays recording
 * is turned off.
 */
@Slf4j
public class PassByRefObjectRecordingConverter implements ObjectRecordingConverter {

    public static final ObjectRecordingConverter INSTANCE = new PassByRefObjectRecordingConverter();

    public Object prepare(Object arg, ConstructedTypesStack constructedObjects) {
        return arg;
    }

    public Object[] prepare(Object[] args, ConstructedTypesStack constructedObjects) {
        return args;
    }
}
