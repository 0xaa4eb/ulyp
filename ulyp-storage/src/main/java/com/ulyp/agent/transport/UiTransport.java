package com.ulyp.agent.transport;

import java.util.concurrent.TimeUnit;

public interface UiTransport {

    void uploadAsync(CallRecordTreeRequest request);

    void shutdownNowAndAwaitForRecordsLogsSending(long time, TimeUnit timeUnit) throws InterruptedException;
}
