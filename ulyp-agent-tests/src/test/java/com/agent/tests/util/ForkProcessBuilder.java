package com.agent.tests.util;

import com.ulyp.agent.Settings;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.core.util.PackageList;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ForkProcessBuilder {

    private final List<SystemProp> systemProps = new ArrayList<>();
    private Class<?> mainClassName;
    private MethodMatcher methodToRecord;
    private String excludeRecordingMethods;
    @Getter
    private OutputFile outputFile = new OutputFile();
    private PackageList instrumentedPackages = new PackageList();
    private String excludeClassesProperty = null;
    private PackageList excludedFromInstrumentationPackages = new PackageList();
    private CollectionsRecordingMode collectionsRecordingMode = CollectionsRecordingMode.NONE;
    private String printClasses = null;
    private String logLevel = "INFO";
    private String recordThreads;
    private Boolean agentDisabled = null;
    private Boolean recordConstructors = null;
    private Boolean instrumentLambdas = null;
    private Boolean instrumentTypeInitializers = null;
    private Boolean recordTimestamps = null;

    public Class<?> getMainClassName() {
        return mainClassName;
    }

    public ForkProcessBuilder withMainClassName(Class<?> mainClassName) {
        this.mainClassName = mainClassName;
        if (instrumentedPackages.isEmpty()) {
            instrumentedPackages = new PackageList(mainClassName.getPackage().getName());
        }
        if (methodToRecord == null) {
            this.methodToRecord = new MethodMatcher(mainClassName, "main");
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

    public ForkProcessBuilder withInstrumentedPackages(String... packages) {
        this.instrumentedPackages = new PackageList(packages);
        return this;
    }

    public ForkProcessBuilder withPrintClasses(String printClasses) {
        this.printClasses = printClasses;
        return this;
    }

    public ForkProcessBuilder withAgentDisabled(Boolean agentDisabled) {
        this.agentDisabled = agentDisabled;
        return this;
    }

    public ForkProcessBuilder withExcludeStartRecordingMethods(String method) {
        this.excludeRecordingMethods = method;
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
            this.methodToRecord = new MethodMatcher(mainClassName, startMethod);
        } else {
            throw new IllegalArgumentException("Please set main class name first");
        }
        return this;
    }

    public ForkProcessBuilder withOutputFile(OutputFile outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    public ForkProcessBuilder withExcludedFromInstrumentationPackages(String... packages) {
        this.excludedFromInstrumentationPackages = new PackageList(packages);
        return this;
    }

    public ForkProcessBuilder withExcludeClassesProperty(String excludeClassesProperty) {
        this.excludeClassesProperty = excludeClassesProperty;
        return this;
    }

    public ForkProcessBuilder withRecordThreads(String recordThreads) {
        this.recordThreads = recordThreads;
        return this;
    }

    public ForkProcessBuilder withSystemProp(SystemProp systemProp) {
        this.systemProps.add(systemProp);
        return this;
    }

    public List<String> toCmdJavaProps() {
        List<String> params = new ArrayList<>();

        params.add("-D" + Settings.PACKAGES_PROPERTY + "=" + String.join(",", instrumentedPackages));
        if (!excludedFromInstrumentationPackages.isEmpty()) {
            params.add("-D" + Settings.EXCLUDE_PACKAGES_PROPERTY + "=" + String.join(",", excludedFromInstrumentationPackages));
        }
        if (excludeClassesProperty != null) {
            params.add("-D" + Settings.EXCLUDE_CLASSES_PROPERTY + "=" + excludeClassesProperty);
        }
        if (printClasses != null) {
            params.add("-D" + Settings.PRINT_TYPES_PROPERTY + "=" + printClasses);
        }
        if (agentDisabled != null && agentDisabled) {
            params.add("-D" + Settings.AGENT_DISABLED_PROPERTY);
        }
        if (instrumentLambdas != null) {
            params.add("-D" + Settings.INSTRUMENT_LAMBDAS_PROPERTY);
        }
        if (recordConstructors != null) {
            params.add("-D" + Settings.INSTRUMENT_CONSTRUCTORS_PROPERTY);
        }
        if (excludeRecordingMethods != null) {
            params.add("-D" + Settings.EXCLUDE_RECORDING_METHODS_PROPERTY + "=" + excludeRecordingMethods);
        }
        if (recordThreads != null) {
            params.add("-D" + Settings.START_RECORDING_THREADS_PROPERTY + "=" + recordThreads);
        }
        if (instrumentTypeInitializers != null) {
            params.add("-D" + Settings.INSTRUMENT_TYPE_INITIALIZERS);
        }
        if (recordTimestamps != null) {
            params.add("-D" + Settings.TIMESTAMPS_ENABLED_PROPERTY);
        }

        params.add("-Dulyp.recording-queue.serialization-buffer-size=" + 2048);
        params.add("-D" + Settings.TYPE_VALIDATION_ENABLED_PROPERTY);
        params.add("-D" + LoggingSettings.LOG_LEVEL_PROPERTY + "=" + logLevel);
        params.add("-D" + Settings.START_RECORDING_METHODS_PROPERTY + "=" + methodToRecord.toString());
        params.add("-D" + Settings.FILE_PATH_PROPERTY + "=" + (outputFile != null ? outputFile : ""));
        params.add("-D" + Settings.RECORD_COLLECTIONS_PROPERTY + "=" + collectionsRecordingMode.name());

        systemProps.forEach(sysProp -> params.add(sysProp.toJavaCmdLineProp()));

        return params;
    }
}
