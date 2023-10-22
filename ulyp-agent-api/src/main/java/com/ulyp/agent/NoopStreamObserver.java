package com.ulyp.agent;

import io.grpc.stub.StreamObserver;

public class NoopStreamObserver<V> implements StreamObserver<V> {
    @Override
    public void onNext(V value) {
    }

    @Override
    public void onError(Throwable t) {
    }

    @Override
    public void onCompleted() {
    }
}
