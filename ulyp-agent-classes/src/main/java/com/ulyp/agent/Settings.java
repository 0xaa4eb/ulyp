package com.ulyp.agent;

import com.ulyp.agent.util.StartRecordingPolicy;
import com.ulyp.core.recorders.CollectionsRecordingMode;
import com.ulyp.core.util.ClassMatcher;
import com.ulyp.core.util.CommaSeparatedList;
import com.ulyp.core.util.PackageList;
import com.ulyp.storage.StorageWriter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Agent settings which define what packages to instrument, at which method recording should start, etc.
 * It's only possible to set settings via JMV system properties at the time.
 */
public class Settings {

    public static final String PACKAGES_PROPERTY = "ulyp.packages";
    public static final String START_RECORDING_DELAY_PROPERTY = "ulyp.delay";
    public static final String EXCLUDE_PACKAGES_PROPERTY = "ulyp.exclude-packages";
    public static final String EXCLUDE_CLASSES_PROPERTY = "ulyp.exclude-classes";
    public static final String START_RECORDING_METHODS_PROPERTY = "ulyp.methods";
    public static final String PRINT_CLASSES_PROPERTY = "ulyp.print-classes";
    public static final String FILE_PATH_PROPERTY = "ulyp.file";
    public static final String RECORD_CONSTRUCTORS_PROPERTY = "ulyp.constructors";
    public static final String RECORD_COLLECTIONS_PROPERTY = "ulyp.collections";
    public static final String AGENT_DISABLED_PROPERTY = "ulyp.off";

    public static Settings fromSystemProperties() {

        Duration delay = Duration.ofSeconds(Integer.parseInt(System.getProperty(START_RECORDING_DELAY_PROPERTY, "0")));
        StartRecordingPolicy startRecordingPolicy = delay.isZero() ? StartRecordingPolicy.alwaysStartRecordingPolicy(): StartRecordingPolicy.withDelayStartRecordingPolicy(delay);

        PackageList instrumentationPackages = new PackageList(CommaSeparatedList.parse(System.getProperty(PACKAGES_PROPERTY, "")));
        PackageList excludedPackages = new PackageList(CommaSeparatedList.parse(System.getProperty(EXCLUDE_PACKAGES_PROPERTY, "")));
        List<ClassMatcher> excludeClassesFromInstrumentation = CommaSeparatedList.parse(System.getProperty(EXCLUDE_CLASSES_PROPERTY, ""))
                .stream()
                .map(ClassMatcher::parse)
                .collect(Collectors.toList());

        String methodsToRecord = System.getProperty(START_RECORDING_METHODS_PROPERTY, "");
        RecordMethodList recordingStartMethods = RecordMethodList.parse(methodsToRecord);

        Supplier<StorageWriter> storageWriterSupplier;
        String filePath = System.getProperty(FILE_PATH_PROPERTY);
        if (filePath != null) {
            if (filePath.isEmpty()) {
                storageWriterSupplier = new Supplier<StorageWriter>() {
                    @Override
                    public StorageWriter get() {
                        return StorageWriter.devNull();
                    }

                    @Override
                    public String toString() {
                        return "dev/null";
                    }
                };
            } else {
                storageWriterSupplier = new Supplier<StorageWriter>() {
                    @Override
                    public StorageWriter get() {
                        return StorageWriter.async(
                                StorageWriter.statsRecording(
                                        StorageWriter.forFile(Paths.get(filePath).toFile())
                                )
                        );
                    }

                    @Override
                    public String toString() {
                        return "file " + filePath;
                    }
                };
            }
        } else {
            throw new RuntimeException("Property " + FILE_PATH_PROPERTY + " must be set");
        }

        boolean shouldRecordConstructors = System.getProperty(RECORD_CONSTRUCTORS_PROPERTY) != null;
        boolean agentDisabled = System.getProperty(AGENT_DISABLED_PROPERTY) != null;

        String recordCollectionsProp = System.getProperty(RECORD_COLLECTIONS_PROPERTY, CollectionsRecordingMode.NONE.name());
        if (recordCollectionsProp.isEmpty()) {
            recordCollectionsProp = CollectionsRecordingMode.ALL.name();
        }
        CollectionsRecordingMode collectionsRecordingMode = CollectionsRecordingMode.valueOf(recordCollectionsProp.toUpperCase());

        Set<ClassMatcher> classesToPrint =
                CommaSeparatedList.parse(System.getProperty(PRINT_CLASSES_PROPERTY, ""))
                    .stream()
                    .map(ClassMatcher::parse)
                    .collect(Collectors.toSet());

        return new Settings(
                storageWriterSupplier,
                instrumentationPackages,
                excludedPackages,
                recordingStartMethods,
                shouldRecordConstructors,
                collectionsRecordingMode,
                classesToPrint,
                startRecordingPolicy,
                excludeClassesFromInstrumentation,
                agentDisabled
        );
    }

    @NotNull private final Supplier<StorageWriter> storageWriterSupplier;
    private final PackageList instrumentatedPackages;
    private final PackageList excludedFromInstrumentationPackages;
    @NotNull private final RecordMethodList recordMethodList;
    private final List<ClassMatcher> excludeFromInstrumentationClasses;
    private final boolean shouldRecordConstructors;
    private final StartRecordingPolicy startRecordingPolicy;
    private final CollectionsRecordingMode collectionsRecordingMode;
    private final Set<ClassMatcher> classesToPrint;
    private final boolean agentDisabled;

    public Settings(
            @NotNull Supplier<StorageWriter> storageWriterSupplier,
            PackageList instrumentedPackages,
            PackageList excludedFromInstrumentationPackages,
            @NotNull RecordMethodList recordMethodList,
            boolean shouldRecordConstructors,
            CollectionsRecordingMode collectionsRecordingMode,
            Set<ClassMatcher> classesToPrint,
            StartRecordingPolicy startRecordingPolicy,
            List<ClassMatcher> excludeFromInstrumentationClasses,
            boolean agentDisabled)
    {
        this.storageWriterSupplier = storageWriterSupplier;
        this.instrumentatedPackages = instrumentedPackages;
        this.excludedFromInstrumentationPackages = excludedFromInstrumentationPackages;
        this.recordMethodList = recordMethodList;
        this.shouldRecordConstructors = shouldRecordConstructors;
        this.collectionsRecordingMode = collectionsRecordingMode;
        this.classesToPrint = classesToPrint;
        this.startRecordingPolicy = startRecordingPolicy;
        this.excludeFromInstrumentationClasses = excludeFromInstrumentationClasses;
        this.agentDisabled = agentDisabled;
    }

    public PackageList getInstrumentatedPackages() {
        return instrumentatedPackages;
    }

    public PackageList getExcludedFromInstrumentationPackages() {
        return excludedFromInstrumentationPackages;
    }

    public RecordMethodList getRecordMethodList() {
        return recordMethodList;
    }

    public StorageWriter buildStorageWriter() {
        return storageWriterSupplier.get();
    }

    public boolean shouldRecordConstructors() {
        return shouldRecordConstructors;
    }

    public CollectionsRecordingMode getCollectionsRecordingMode() {
        return collectionsRecordingMode;
    }

    public Set<ClassMatcher> getClassesToPrint() {
        return classesToPrint;
    }

    public StartRecordingPolicy getStartRecordingPolicy() {
        return startRecordingPolicy;
    }

    public List<ClassMatcher> getExcludeFromInstrumentationClasses() {
        return excludeFromInstrumentationClasses;
    }

    public boolean isAgentDisabled() {
        return agentDisabled;
    }

    @Override
    public String toString() {
        return "file: " + storageWriterSupplier +
                ",\npackages to instrument: " + instrumentatedPackages +
                ",\npackages excluded from instrumentation: " + excludedFromInstrumentationPackages +
                ",\nstart recording at methods: " + recordMethodList +
                ",\nrecord constructors: " + shouldRecordConstructors +
                ",\nrecording policy: " + startRecordingPolicy +
                ",\nrecord collections: " + collectionsRecordingMode +
                ",\nclassesToPrintWithToString(TBD)=" + classesToPrint;
    }
}
