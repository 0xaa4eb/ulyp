package com.ulyp.ui.util

import javafx.application.Platform
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Supplier

object FxThreadExecutor {
    @JvmStatic
    fun <T> execute(supplier: Supplier<T>): T {
        val future = CompletableFuture<T>()
        Platform.runLater {
            try {
                future.complete(supplier.get())
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return try {
            future[1, TimeUnit.MINUTES]
        } catch (e: InterruptedException) {
            throw RuntimeException("Could not execute supplier $supplier", e)
        } catch (e: ExecutionException) {
            throw RuntimeException("Could not execute supplier $supplier", e)
        } catch (e: TimeoutException) {
            throw RuntimeException("Could not execute supplier $supplier", e)
        }
    }
}