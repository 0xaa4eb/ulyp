package com.ulyp.agent;

import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import com.ulyp.core.util.ClassMatcher;
import com.ulyp.core.util.CommaSeparatedList;
import com.ulyp.core.util.PackageList;
import com.ulyp.storage.StorageWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
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
    public static final String START_RECORDING_POLICY_PROPERTY = "ulyp.policy";
    public static final String BIND_NETWORK_ADDRESS = "ulyp.bind";
    public static final String EXCLUDE_PACKAGES_PROPERTY = "ulyp.exclude-packages";
    public static final String EXCLUDE_CLASSES_PROPERTY = "ulyp.exclude-classes";
    public static final String EXCLUDE_RECORDING_METHODS_PROPERTY = "ulyp.exclude-methods";
    public static final String START_RECORDING_METHODS_PROPERTY = "ulyp.methods";
    public static final String PRINT_CLASSES_PROPERTY = "ulyp.print-classes";
    public static final String FILE_PATH_PROPERTY = "ulyp.file";
    public static final String INSTRUMENT_CONSTRUCTORS_PROPERTY = "ulyp.constructors";
    public static final String INSTRUMENT_LAMBDAS_PROPERTY = "ulyp.lambdas";
    public static final String RECORD_COLLECTIONS_PROPERTY = "ulyp.collections";
    public static final String AGGRESSIVE_PROPERTY = "ulyp.aggressive";
    public static final String AGENT_DISABLED_PROPERTY = "ulyp.off";

    @NotNull
    private final Supplier<StorageWriter> storageWriterSupplier;
    private final PackageList instrumentatedPackages;
    private final PackageList excludedFromInstrumentationPackages;
    @NotNull
    private final StartRecordingMethods startRecordingMethods;
    private final List<ClassMatcher> excludeFromInstrumentationClasses;
    private final boolean instrumentConstructors;
    private final boolean instrumentLambdas;
    private final String startRecordingPolicyPropertyValue;
    private final CollectionsRecordingMode collectionsRecordingMode;
    private final Set<ClassMatcher> classesToPrint;
    private final String bindNetworkAddress;
    private final boolean agentDisabled;

    public Settings(
            @NotNull Supplier<StorageWriter> storageWriterSupplier,
            PackageList instrumentedPackages,
            PackageList excludedFromInstrumentationPackages,
            @NotNull StartRecordingMethods startRecordingMethods,
            boolean instrumentConstructors,
            boolean instrumentLambdas,
            CollectionsRecordingMode collectionsRecordingMode,
            Set<ClassMatcher> classesToPrint,
            String startRecordingPolicyPropertyValue,
            List<ClassMatcher> excludeFromInstrumentationClasses,
            String bindNetworkAddress,
            boolean agentDisabled) {
        this.storageWriterSupplier = storageWriterSupplier;
        this.instrumentatedPackages = instrumentedPackages;
        this.excludedFromInstrumentationPackages = excludedFromInstrumentationPackages;
        this.startRecordingMethods = startRecordingMethods;
        this.instrumentConstructors = instrumentConstructors;
        this.instrumentLambdas = instrumentLambdas;
        this.collectionsRecordingMode = collectionsRecordingMode;
        this.classesToPrint = classesToPrint;
        this.startRecordingPolicyPropertyValue = startRecordingPolicyPropertyValue;
        this.excludeFromInstrumentationClasses = excludeFromInstrumentationClasses;
        this.bindNetworkAddress = bindNetworkAddress;
        this.agentDisabled = agentDisabled;
    }

    public static Settings fromSystemProperties() {

        String startRecordingPolicy = System.getProperty(START_RECORDING_POLICY_PROPERTY);
        String bindNetworkAddress = System.getProperty(BIND_NETWORK_ADDRESS);

        PackageList instrumentationPackages = new PackageList(CommaSeparatedList.parse(System.getProperty(PACKAGES_PROPERTY, "")));
        PackageList excludedPackages = new PackageList(CommaSeparatedList.parse(System.getProperty(EXCLUDE_PACKAGES_PROPERTY, "")));
        List<ClassMatcher> excludeClassesFromInstrumentation = CommaSeparatedList.parse(System.getProperty(EXCLUDE_CLASSES_PROPERTY, ""))
                .stream()
                .map(ClassMatcher::parse)
                .collect(Collectors.toList());

        String methodsToRecordRaw = System.getProperty(START_RECORDING_METHODS_PROPERTY, "");
        String excludeMethodsToRecordRaw = System.getProperty(EXCLUDE_RECORDING_METHODS_PROPERTY, "");
        StartRecordingMethods recordingStartMethods = StartRecordingMethods.parse(methodsToRecordRaw, excludeMethodsToRecordRaw);

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

        boolean aggressive = System.getProperty(AGGRESSIVE_PROPERTY) != null;
        boolean recordConstructors = aggressive || System.getProperty(INSTRUMENT_CONSTRUCTORS_PROPERTY) != null;
        boolean instrumentLambdas = aggressive || System.getProperty(INSTRUMENT_LAMBDAS_PROPERTY) != null;

        String recordCollectionsProp;
        if (aggressive) {
            recordCollectionsProp = CollectionsRecordingMode.JAVA.name();
        } else {
            recordCollectionsProp = System.getProperty(RECORD_COLLECTIONS_PROPERTY, CollectionsRecordingMode.NONE.name());
            if (recordCollectionsProp.isEmpty()) {
                recordCollectionsProp = CollectionsRecordingMode.ALL.name();
            }
        }
        CollectionsRecordingMode collectionsRecordingMode = CollectionsRecordingMode.valueOf(recordCollectionsProp.toUpperCase());

        boolean agentDisabled = System.getProperty(AGENT_DISABLED_PROPERTY) != null;

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
                recordConstructors,
                instrumentLambdas,
                collectionsRecordingMode,
                classesToPrint,
                startRecordingPolicy,
                excludeClassesFromInstrumentation,
                bindNetworkAddress,
                agentDisabled
        );
    }

    @Nullable
    public String getBindNetworkAddress() {
        return bindNetworkAddress;
    }

    public PackageList getInstrumentatedPackages() {
        return instrumentatedPackages;
    }

    public PackageList getExcludedFromInstrumentationPackages() {
        return excludedFromInstrumentationPackages;
    }

    public boolean instrumentLambdas() {
        return instrumentLambdas;
    }

    @NotNull
    public StartRecordingMethods getRecordMethodList() {
        return startRecordingMethods;
    }

    public StorageWriter buildStorageWriter() {
        return storageWriterSupplier.get();
    }

    public boolean instrumentConstructors() {
        return instrumentConstructors;
    }

    public CollectionsRecordingMode getCollectionsRecordingMode() {
        return collectionsRecordingMode;
    }

    public Set<ClassMatcher> getClassesToPrint() {
        return classesToPrint;
    }

    public String getStartRecordingPolicyPropertyValue() {
        return startRecordingPolicyPropertyValue;
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
                ",\nstart recording at methods: " + startRecordingMethods +
                ",\ninstrument constructors: " + instrumentConstructors +
                ",\ninstrument lambdas: " + instrumentLambdas +
                ",\nrecording policy: " + startRecordingPolicyPropertyValue +
                ",\nrecord collections: " + collectionsRecordingMode +
                ",\nclassesToPrintWithToString(TBD)=" + classesToPrint;
    }
}
