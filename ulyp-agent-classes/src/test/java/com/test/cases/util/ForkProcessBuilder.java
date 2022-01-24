package com.test.cases.util;

import com.ulyp.agent.Settings;
import com.ulyp.core.recorders.CollectionsRecordingMode;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.core.util.PackageList;

import java.util.ArrayList;
import java.util.List;

public class ForkProcessBuilder {

    private Class<?> mainClassName;
    private MethodMatcher methodToRecord;
    private OutputFile outputFile = new OutputFile("test", ".dat");
    private PackageList instrumentedPackages = new PackageList();
    private PackageList excludedFromInstrumentationPackages = new PackageList();
    private CollectionsRecordingMode collectionsRecordingMode = CollectionsRecordingMode.NONE;
    private String logLevel = "INFO";

    public Class<?> getMainClassName() {
        return mainClassName;
    }

    public ForkProcessBuilder setMainClassName(Class<?> mainClassName) {
        this.mainClassName = mainClassName;
        if (instrumentedPackages.isEmpty()) {
            instrumentedPackages = new PackageList(mainClassName.getPackage().getName());
        }
        if (methodToRecord == null) {
            this.methodToRecord = new MethodMatcher(mainClassName, "main");
        }
        return this;
    }

    public ForkProcessBuilder recordCollections(CollectionsRecordingMode mode) {
        collectionsRecordingMode = mode;
        return this;
    }

    public ForkProcessBuilder setInstrumentedPackages(String... packages) {
        this.instrumentedPackages = new PackageList(packages);
        return this;
    }

    public ForkProcessBuilder setMethodToRecord(MethodMatcher methodToRecord) {
        this.methodToRecord = methodToRecord;
        return this;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public ForkProcessBuilder setLogLevel(String logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public ForkProcessBuilder setMethodToRecord(String startMethod) {
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

    public ForkProcessBuilder setOutputFile(OutputFile outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    public ForkProcessBuilder setExcludedFromInstrumentationPackages(String... packages) {
        this.excludedFromInstrumentationPackages = new PackageList(packages);
        return this;
    }

    public List<String> toCmdJavaProps() {
        List<String> params = new ArrayList<>();

        params.add("-D" + Settings.PACKAGES_PROPERTY + "=" + String.join(",", instrumentedPackages));
        if (!excludedFromInstrumentationPackages.isEmpty()) {
            params.add("-D" + Settings.EXCLUDE_PACKAGES_PROPERTY + "=" + String.join(",", excludedFromInstrumentationPackages));
        }

        params.add("-D" + Settings.START_RECORDING_METHODS_PROPERTY + "=" + methodToRecord.toString());
        params.add("-D" + Settings.FILE_PATH_PROPERTY + "=" + (outputFile != null ? outputFile : ""));
        params.add("-D" + Settings.RECORD_COLLECTIONS_PROPERTY + "=" + collectionsRecordingMode.name());

        return params;
    }
}
