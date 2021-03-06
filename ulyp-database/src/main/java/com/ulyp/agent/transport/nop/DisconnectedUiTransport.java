package com.ulyp.agent.transport.nop;

import com.ulyp.agent.transport.CallRecordTreeRequest;
import com.ulyp.agent.transport.UiTransport;

import java.util.concurrent.TimeUnit;

public class DisconnectedUiTransport implements UiTransport {


    public DisconnectedUiTransport() {
    }

    @Override
    public void uploadAsync(CallRecordTreeRequest request) {
        // NOP
        System.out.println("Won't send " + request.getRecordLog().size() + " records");
    }

    @Override
    public void shutdownNowAndAwaitForRecordsLogsSending(long time, TimeUnit timeUnit) throws InterruptedException {

        // NOP
    }
}
