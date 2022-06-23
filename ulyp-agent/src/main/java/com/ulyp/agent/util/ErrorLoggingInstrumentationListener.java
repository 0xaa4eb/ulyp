package com.ulyp.agent.util;

import com.ulyp.core.util.LoggingSettings;
import com.ulyp.storage.util.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.ulyp.core.util.LoggingSettings.LOG_LEVEL_PROPERTY;

@Slf4j
public class ErrorLoggingInstrumentationListener implements AgentBuilder.Listener {

    private static final Duration ERROR_DUMP_INTERVAL = Duration.ofSeconds(10);

    private final InstrumentationErrors errors = new InstrumentationErrors();

    public ErrorLoggingInstrumentationListener() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
                NamedThreadFactory.builder()
                        .name("InstrumentationErrorsLoggingThread")
                        .daemon(true) // daemon executor service, do not need to close it
                        .build()
        );

        executor.scheduleAtFixedRate(
                this::dumpErrors,
                ERROR_DUMP_INTERVAL.toMillis(),
                ERROR_DUMP_INTERVAL.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

    }

    @Override
    public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {

    }

    @Override
    public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {

    }

    @Override
    public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
        if (LoggingSettings.DEBUG_ENABLED) {
            log.debug("Failed to instrument class " + typeName, throwable);
        }
        errors.onError();
    }

    private void dumpErrors() {
        long errorCount = errors.getErrorCountAndReset();
        if (errorCount > 0) {
            log.info("There were {} instrumentation errors. You may want to enable " +
                    "debug log level using {} system prop to check actual errors",
                    errorCount,
                    LOG_LEVEL_PROPERTY
            );
        }
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

    }

    private static class InstrumentationErrors {

        private long errorsCount = 0L;

        public synchronized void onError() {
            errorsCount++;
        }

        public synchronized long getErrorCountAndReset() {
            long value = errorsCount;
            errorsCount = 0L;
            return value;
        }
    }
}
