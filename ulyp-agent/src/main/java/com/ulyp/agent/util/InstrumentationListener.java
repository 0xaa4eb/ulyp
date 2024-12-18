package com.ulyp.agent.util;

import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.util.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A listener which is called by byte-buddy library
 */
@Slf4j
public class InstrumentationListener implements AgentBuilder.Listener {

    private static final Duration ERROR_DUMP_INTERVAL = Duration.ofSeconds(10);

    private final AtomicReference<Errors> errors = new AtomicReference<>(new Errors());

    public InstrumentationListener() {
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
        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Transformed {}", typeDescription);
        }
    }

    @Override
    public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Instrumentation ignored {}", typeDescription);
        }
    }

    @Override
    public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
        if (LoggingSettings.DEBUG_ENABLED) {
            log.debug("Failed to instrument class " + typeName, throwable);
        }
        errors.get().onError(typeName, throwable);
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

    }

    private void dumpErrors() {
        Errors errors = this.errors.getAndSet(new Errors());
        if (!errors.isEmpty()) {
            log.info("There were {} instrumentation errors. Some types: {}", errors.errorsCount, errors.errors);
        }
    }

    private static class Error {

        private final String typeName;
        private final Throwable throwable;

        private Error(String typeName, Throwable throwable) {
            this.typeName = typeName;
            this.throwable = throwable;
        }

        @Override
        public String toString() {
            return "Error{typeName='" + typeName + '\'' + ", throwable=" + throwable + '}';
        }
    }

    @ThreadSafe
    private static class Errors {

        private static final int MAX_ERRORS_STORED = 10;

        private int errorsCount;
        private final List<Error> errors = new ArrayList<>();

        public synchronized void onError(String typeName, Throwable throwable) {
            errorsCount++;
            if (errors.size() < MAX_ERRORS_STORED) {
                errors.add(new Error(typeName, throwable));
            }
        }

        public boolean isEmpty() {
            return errorsCount == 0;
        }
    }
}
