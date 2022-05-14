package com.agent.tests.util;

import com.ulyp.agent.Settings;
import com.ulyp.core.recorders.CollectionsRecordingMode;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.core.util.PackageList;

import java.util.ArrayList;
import java.util.List;

public class ForkProcessBuilder {

    private Class<?> mainClassName;
    private MethodMatcher methodToRecord;
    private OutputFile outputFile = new OutputFile();
    private PackageList instrumentedPackages = new PackageList();
    private String excludeClassesProperty = null;
    private PackageList excludedFromInstrumentationPackages = new PackageList();
    private CollectionsRecordingMode collectionsRecordingMode = CollectionsRecordingMode.NONE;
    private String printClasses = null;
    private String logLevel = "INFO";
    private Boolean agentDisabled = null;
    private final List<SystemProp> systemProps = new ArrayList<>();

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

    public ForkProcessBuilder withMethodToRecord(MethodMatcher methodToRecord) {
        this.methodToRecord = methodToRecord;
        return this;
    }

    public String getLogLevel() {
        return logLevel;
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

    public OutputFile getOutputFile() {
        return outputFile;
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
            params.add("-D" + Settings.PRINT_CLASSES_PROPERTY + "=" + printClasses);
        }
        if (agentDisabled != null && agentDisabled) {
            params.add("-D" + Settings.AGENT_DISABLED_PROPERTY);
        }

        params.add("-D" + Settings.START_RECORDING_METHODS_PROPERTY + "=" + methodToRecord.toString());
        params.add("-D" + Settings.FILE_PATH_PROPERTY + "=" + (outputFile != null ? outputFile : ""));
        params.add("-D" + Settings.RECORD_COLLECTIONS_PROPERTY + "=" + collectionsRecordingMode.name());

        systemProps.forEach(sysProp -> params.add(sysProp.toJavaCmdLineProp()));

        return params;
    }
}
