package com.ulyp.agent;

import com.ulyp.core.exception.UlypException;
import io.grpc.stub.StreamObserver;

public class NoopStreamObserver<V> implements StreamObserver<V> {
    @Override
    public void onNext(V value) {
    }

    @Override
    public void onError(Throwable t) {
        throw new UlypException(t);
    }

    @Override
    public void onCompleted() {
    }
}
