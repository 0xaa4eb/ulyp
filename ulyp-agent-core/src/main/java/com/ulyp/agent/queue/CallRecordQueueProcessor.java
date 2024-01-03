package com.ulyp.agent.queue;

import com.lmax.disruptor.EventHandler;

public class CallRecordQueueProcessor implements EventHandler<CallRecordQueueEntry> {



    @Override
    public void onEvent(CallRecordQueueEntry event, long sequence, boolean endOfBatch) throws Exception {

    }
}
