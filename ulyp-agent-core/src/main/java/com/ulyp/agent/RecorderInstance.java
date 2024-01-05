package com.ulyp.agent;

/**
 * A {@link Recorder} instance must be accessed from advice, so it must be kept static somewhere.
 */
public class RecorderInstance {

    public static final Recorder instance = AgentContext.getCtx().getRecorder();
}
