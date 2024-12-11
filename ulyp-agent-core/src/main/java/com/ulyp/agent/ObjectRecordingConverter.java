package com.ulyp.agent;

import com.ulyp.agent.util.ConstructedTypesStack;

/**
 * Prepares (and possibly converts) object for sending to the background thread. The background thread does actual recording using the recorders.
 * Most of the time we just pass reference to an object to the background thread which calls recorders. This allows us to
 * offload some work from the client app threads to the background thread.
 * This works since most objects are either:
 * 1) immutable
 * 2) we only record their type id and identity hash code.
 * So, we can record their values (i.e. access their fields) in some other threads.
 * All other objects like collections and arrays must be recorded immediately in the client app thread.
 */
public interface ObjectRecordingConverter {

    Object prepare(Object arg, ConstructedTypesStack constructedObjects);

    Object[] prepare(Object[] args, ConstructedTypesStack constructedObjects);
}
