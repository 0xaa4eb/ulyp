package com.ulyp.agent.queue;

import java.util.HashMap;
import java.util.Map;

import com.lmax.disruptor.EventHandler;
import com.ulyp.core.CallRecordBuffer;

public class CallRecordQueueProcessor implements EventHandler<EnterRecordQueueItem> {

    private final Map<Integer, CallRecordBuffer> recordBuffer = new HashMap<>();

    @Override
    public void onEvent(EnterRecordQueueItem event, long sequence, boolean endOfBatch) throws Exception {

        recordBuffer.get(event.)
    }
}
