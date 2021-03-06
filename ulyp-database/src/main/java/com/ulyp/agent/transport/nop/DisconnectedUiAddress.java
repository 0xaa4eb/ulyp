package com.ulyp.agent.transport.nop;

import com.ulyp.agent.transport.UiAddress;
import com.ulyp.agent.transport.UiTransport;

/**
 * Only used when UI is disabled explicitly. Users will probably never disable UI, mostly this is used
 * in benchmarks only
 */
public class DisconnectedUiAddress implements UiAddress {

    public DisconnectedUiAddress() {
    }

    @Override
    public UiTransport buildTransport() {
        return new DisconnectedUiTransport();
    }
}
