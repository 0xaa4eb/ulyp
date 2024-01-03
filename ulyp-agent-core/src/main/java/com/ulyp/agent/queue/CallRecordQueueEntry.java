package com.ulyp.agent.queue;

class CallRecordQueueEntry {

    private boolean enterCall;
    private int callId;
    private int methodId;
    private Object callee;
    private boolean thrown;
    private Object[] args;
    private long nanoTime;

    void clear() {

    }

    void setEnterCall(int callId, int methodId, Object callee, Object[] args) {
        this.callId = callId;
        this.methodId = methodId;
        this.callee = callee;
        this.args = args;
        this.enterCall = true;
    }

    void setExitCall(int callId, Object returnValue, boolean thrown) {
        this.callId = callId;
        this.callee = returnValue;
        this.thrown = thrown;
        this.enterCall = false;
    }
}
