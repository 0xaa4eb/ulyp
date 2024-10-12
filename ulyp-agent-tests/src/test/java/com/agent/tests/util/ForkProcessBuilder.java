package com.agent.tests.util;

import com.ulyp.agent.options.AgentOptions;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.core.util.SingleMethodMatcher;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForkProcessBuilder {

    private final List<SystemProp> systemProps = new ArrayList<>();
    @Getter
    private Class<?> mainClassName;
    private MethodMatcher methodToRecord;
    @Getter
    private OutputFile outputFile = new OutputFile();
    private List<String> instrumentedPackages = new ArrayList<>();
    private String excludeClassesProperty = null;
    private List<String> excludedFromInstrumentationPackages = new ArrayList<>();
    private CollectionsRecordingMode collectionsRecordingMode = CollectionsRecordingMode.NONE;
    private String printClasses = null;
    private String logLevel = "INFO";
    private Boolean agentDisabled = null;
    private Boolean recordConstructors = null;
    private Boolean instrumentLambdas = null;
    private Boolean instrumentTypeInitializers = null;
    private Boolean recordTimestamps = null;
    private Boolean recordArrays = null;

    public ForkProcessBuilder withMainClassName(Class<?> mainClassName) {
        this.mainClassName = mainClassName;
        if (instrumentedPackages.isEmpty()) {
            instrumentedPackages = Arrays.asList(mainClassName.getPackage().getName());
        }
        if (methodToRecord == null) {
            this.methodToRecord = new SingleMethodMatcher(mainClassName, "main");
        }
        return this;
    }

    public ForkProcessBuilder withRecordCollections(CollectionsRecordingMode mode) {
        collectionsRecordingMode = mode;
        return this;
    }

    public ForkProcessBuilder withRecordConstructors() {
        recordConstructors = true;
        return this;
    }

    public ForkProcessBuilder withRecordArrays() {
        recordArrays = true;
        return this;
    }

    public ForkProcessBuilder withInstrumentedPackages(String... packages) {
        this.instrumentedPackages = Arrays.asList(packages);
        return this;
    }

    public ForkProcessBuilder withPrintClasses(String printClasses) {
        this.printClasses = printClasses;
        return this;
    }

    public ForkProcessBuilder withMethodToRecord(MethodMatcher methodToRecord) {
        this.methodToRecord = methodToRecord;
        return this;
    }

    public ForkProcessBuilder withInstrumentLambdas(Boolean instrumentLambdas) {
        this.instrumentLambdas = instrumentLambdas;
        return this;
    }

    public ForkProcessBuilder withRecordTimestamps(Boolean recordTimestamps) {
        this.recordTimestamps = recordTimestamps;
        return this;
    }

    public ForkProcessBuilder withInstrumentTypeInitializers(Boolean instrumentTypeInitializers) {
        this.instrumentTypeInitializers = instrumentTypeInitializers;
        return this;
    }

    public ForkProcessBuilder withLogLevel(String logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public ForkProcessBuilder withMethodToRecord(String startMethod) {
        if (mainClassName != null) {
            this.methodToRecord = new SingleMethodMatcher(mainClassName, startMethod);
        } else {
            throw new IllegalArgumentException("Please set main class name first");
        }
        return this;
    }

    public ForkProcessBuilder withExcludedFromInstrumentationPackages(String... packages) {
        this.excludedFromInstrumentationPackages = Arrays.asList(packages);
        return this;
    }

    public ForkProcessBuilder withExcludeClassesProperty(String excludeClassesProperty) {
        this.excludeClassesProperty = excludeClassesProperty;
        return this;
    }

    public ForkProcessBuilder withSystemProp(SystemProp systemProp) {
        this.systemProps.add(systemProp);
        return this;
    }

    public List<String> toCmdJavaProps() {
        List<String> params = new ArrayList<>();

        params.add("-D" + AgentOptions.PACKAGES_PROPERTY + "=" + String.join(",", instrumentedPackages));
        if (!excludedFromInstrumentationPackages.isEmpty()) {
            params.add("-D" + AgentOptions.EXCLUDE_PACKAGES_PROPERTY + "=" + String.join(",", excludedFromInstrumentationPackages));
        }
        if (excludeClassesProperty != null) {
            params.add("-D" + AgentOptions.EXCLUDE_CLASSES_PROPERTY + "=" + excludeClassesProperty);
        }
        if (printClasses != null) {
            params.add("-D" + AgentOptions.PRINT_TYPES_PROPERTY + "=" + printClasses);
        }
        if (agentDisabled != null && agentDisabled) {
            params.add("-D" + AgentOptions.AGENT_DISABLED_PROPERTY);
        }
        if (instrumentLambdas != null) {
            params.add("-D" + AgentOptions.INSTRUMENT_LAMBDAS_PROPERTY);
        }
        if (recordConstructors != null) {
            params.add("-D" + AgentOptions.INSTRUMENT_CONSTRUCTORS_PROPERTY);
        }
        if (instrumentTypeInitializers != null) {
            params.add("-D" + AgentOptions.INSTRUMENT_TYPE_INITIALIZERS);
        }
        if (recordTimestamps != null) {
            params.add("-D" + AgentOptions.TIMESTAMPS_ENABLED_PROPERTY);
        }
        if (recordArrays != null) {
            params.add("-D" + AgentOptions.RECORD_ARRAYS_PROPERTY);
        }

        params.add("-Dulyp.recording-queue.serialization-buffer-size=" + 2048);
        params.add("-D" + AgentOptions.TYPE_VALIDATION_ENABLED_PROPERTY);
        params.add("-D" + LoggingSettings.LOG_LEVEL_PROPERTY + "=" + logLevel);
        params.add("-D" + AgentOptions.START_RECORDING_METHODS_PROPERTY + "=" + methodToRecord.toString());
        params.add("-D" + AgentOptions.FILE_PATH_PROPERTY + "=" + (outputFile != null ? outputFile : ""));
        params.add("-D" + AgentOptions.RECORD_COLLECTIONS_PROPERTY + "=" + collectionsRecordingMode.name());

        systemProps.forEach(sysProp -> params.add(sysProp.toJavaCmdLineProp()));

        return params;
    }
}
