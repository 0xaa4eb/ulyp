package com.ulyp.ui.util;

import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class FxThreadExecutor {

    public static <T> T execute(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();

        Platform.runLater(() -> {
            try {
                future.complete(supplier.get());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        try {
            return future.get(1, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Could not execute supplier " + supplier, e);
        }
    }
}
