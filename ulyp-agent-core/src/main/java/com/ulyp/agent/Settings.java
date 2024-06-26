package com.ulyp.agent;

import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import com.ulyp.core.util.TypeMatcher;
import com.ulyp.core.util.CommaSeparatedList;
import com.ulyp.core.util.PackageList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Agent settings which define what packages to instrument, at which method recording should start, etc.
 * It's only possible to set settings via JMV system properties at the time.
 */
@Getter
public class Settings {

    public static final boolean TIMESTAMPS_ENABLED;

    public static final String PACKAGES_PROPERTY = "ulyp.packages";
    public static final String START_RECORDING_POLICY_PROPERTY = "ulyp.policy";
    public static final String BIND_NETWORK_ADDRESS = "ulyp.bind";
    public static final String EXCLUDE_PACKAGES_PROPERTY = "ulyp.exclude-packages";
    public static final String EXCLUDE_CLASSES_PROPERTY = "ulyp.exclude-classes";
    public static final String EXCLUDE_RECORDING_METHODS_PROPERTY = "ulyp.exclude-methods";
    public static final String START_RECORDING_METHODS_PROPERTY = "ulyp.methods";
    public static final String START_RECORDING_THREADS_PROPERTY = "ulyp.threads";
    public static final String PRINT_TYPES_PROPERTY = "ulyp.print";
    public static final String FILE_PATH_PROPERTY = "ulyp.file";
    public static final String INSTRUMENT_CONSTRUCTORS_PROPERTY = "ulyp.constructors";
    public static final String INSTRUMENT_LAMBDAS_PROPERTY = "ulyp.lambdas";
    public static final String INSTRUMENT_TYPE_INITIALIZERS = "ulyp.type-initializers";
    public static final String RECORD_COLLECTIONS_PROPERTY = "ulyp.collections";
    public static final String AGGRESSIVE_PROPERTY = "ulyp.aggressive";
    public static final String TIMESTAMPS_ENABLED_PROPERTY = "ulyp.timestamps";
    public static final String TYPE_VALIDATION_ENABLED_PROPERTY = "ulyp.type-validation";
    public static final String AGENT_DISABLED_PROPERTY = "ulyp.off";
    public static final String METRICS_ENABLED_PROPERTY = "ulyp.metrics";

    static {
        // make 'static final'. bytecode will be thrown off if the feature is disabled
        TIMESTAMPS_ENABLED = System.getProperty(TIMESTAMPS_ENABLED_PROPERTY) != null;
    }

    @NotNull
    private final String recordingDataFilePath;
    private final PackageList instrumentatedPackages;
    private final PackageList excludedFromInstrumentationPackages;
    @NotNull
    private final StartRecordingMethods startRecordingMethods;
    private final Pattern startRecordingThreads;
    private final List<TypeMatcher> excludeFromInstrumentationClasses;
    private final boolean instrumentConstructorsEnabled;
    private final boolean instrumentLambdasEnabled;
    private final boolean instrumentTypeInitializers;
    private final String startRecordingPolicyPropertyValue;
    private final CollectionsRecordingMode collectionsRecordingMode;
    private final Set<TypeMatcher> typesToPrint;
    private final String bindNetworkAddress;
    private final boolean agentEnabled;
    private final boolean timestampsEnabled;
    private final boolean metricsEnabled;
    private final boolean typeValidationEnabled;

    public Settings(
            @NotNull String recordingDataFilePath,
            PackageList instrumentedPackages,
            PackageList excludedFromInstrumentationPackages,
            @NotNull StartRecordingMethods startRecordingMethods,
            Pattern startRecordingThreads,
            boolean instrumentConstructorsEnabled,
            boolean instrumentLambdasEnabled,
            boolean instrumentTypeInitializers,
            CollectionsRecordingMode collectionsRecordingMode,
            Set<TypeMatcher> typesToPrint,
            String startRecordingPolicyPropertyValue,
            List<TypeMatcher> excludeFromInstrumentationClasses,
            String bindNetworkAddress,
            boolean agentEnabled,
            boolean timestampsEnabled,
            boolean metricsEnabled,
            boolean typeValidationEnabled) {
        this.recordingDataFilePath = recordingDataFilePath;
        this.instrumentatedPackages = instrumentedPackages;
        this.excludedFromInstrumentationPackages = excludedFromInstrumentationPackages;
        this.startRecordingMethods = startRecordingMethods;
        this.startRecordingThreads = startRecordingThreads;
        this.instrumentConstructorsEnabled = instrumentConstructorsEnabled;
        this.instrumentLambdasEnabled = instrumentLambdasEnabled;
        this.instrumentTypeInitializers = instrumentTypeInitializers;
        this.collectionsRecordingMode = collectionsRecordingMode;
        this.typesToPrint = typesToPrint;
        this.startRecordingPolicyPropertyValue = startRecordingPolicyPropertyValue;
        this.excludeFromInstrumentationClasses = excludeFromInstrumentationClasses;
        this.bindNetworkAddress = bindNetworkAddress;
        this.agentEnabled = agentEnabled;
        this.timestampsEnabled = timestampsEnabled;
        this.metricsEnabled = metricsEnabled;
        this.typeValidationEnabled = typeValidationEnabled;
    }

    public static Settings fromSystemProperties() {

        String startRecordingPolicy = System.getProperty(START_RECORDING_POLICY_PROPERTY);
        String bindNetworkAddress = System.getProperty(BIND_NETWORK_ADDRESS);

        PackageList instrumentationPackages = new PackageList(CommaSeparatedList.parse(System.getProperty(PACKAGES_PROPERTY, "")));
        PackageList excludedPackages = new PackageList(CommaSeparatedList.parse(System.getProperty(EXCLUDE_PACKAGES_PROPERTY, "")));
        List<TypeMatcher> excludeClassesFromInstrumentation = CommaSeparatedList.parse(System.getProperty(EXCLUDE_CLASSES_PROPERTY, ""))
                .stream()
                .map(TypeMatcher::parse)
                .collect(Collectors.toList());

        String methodsToRecordRaw = System.getProperty(START_RECORDING_METHODS_PROPERTY, "");
        String excludeMethodsToRecordRaw = System.getProperty(EXCLUDE_RECORDING_METHODS_PROPERTY, "");
        StartRecordingMethods recordingStartMethods = StartRecordingMethods.parse(methodsToRecordRaw, excludeMethodsToRecordRaw);

        String recordingDataFilePath = System.getProperty(FILE_PATH_PROPERTY);
        if (recordingDataFilePath == null) {
            throw new RuntimeException("Property " + FILE_PATH_PROPERTY + " must be set");
        }

        Pattern recordThreads = Optional.ofNullable(System.getProperty(START_RECORDING_THREADS_PROPERTY))
                .map(Pattern::compile)
                .orElse(null);

        boolean aggressive = System.getProperty(AGGRESSIVE_PROPERTY) != null;
        boolean recordConstructors = aggressive || System.getProperty(INSTRUMENT_CONSTRUCTORS_PROPERTY) != null;
        boolean instrumentLambdas = aggressive || System.getProperty(INSTRUMENT_LAMBDAS_PROPERTY) != null;
        boolean instrumentTypeInitializers = aggressive || System.getProperty(INSTRUMENT_TYPE_INITIALIZERS) != null;
        boolean timestampsEnabled = System.getProperty(TIMESTAMPS_ENABLED_PROPERTY) != null;

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

        boolean agentEnabled = System.getProperty(AGENT_DISABLED_PROPERTY) == null;
        boolean metricsEnabled = System.getProperty(METRICS_ENABLED_PROPERTY) != null;
        boolean typeValidationEnabled = System.getProperty(TYPE_VALIDATION_ENABLED_PROPERTY) != null;

        Set<TypeMatcher> typesToPrint =
                CommaSeparatedList.parse(System.getProperty(PRINT_TYPES_PROPERTY, ""))
                        .stream()
                        .map(TypeMatcher::parse)
                        .collect(Collectors.toSet());

        return new Settings(
                recordingDataFilePath,
                instrumentationPackages,
                excludedPackages,
                recordingStartMethods,
                recordThreads,
                recordConstructors,
                instrumentLambdas,
                instrumentTypeInitializers,
                collectionsRecordingMode,
                typesToPrint,
                startRecordingPolicy,
                excludeClassesFromInstrumentation,
                bindNetworkAddress,
                agentEnabled,
                timestampsEnabled,
                metricsEnabled,
                typeValidationEnabled
        );
    }

    @Nullable
    public String getBindNetworkAddress() {
        return bindNetworkAddress;
    }

    @NotNull
    public StartRecordingMethods getRecordMethodList() {
        return startRecordingMethods;
    }

    @Nullable
    public Pattern getStartRecordingThreads() {
        return startRecordingThreads;
    }

    public boolean instrumentTypeInitializers() {
        return instrumentTypeInitializers;
    }

    @Override
    public String toString() {
        return "file: " + recordingDataFilePath +
                ",\npackages to instrument: " + instrumentatedPackages +
                ",\npackages excluded from instrumentation: " + excludedFromInstrumentationPackages +
                ",\nstart recording at methods: " + startRecordingMethods +
                (startRecordingThreads != null ? (",\nstart recording at threads: " + startRecordingThreads) : "") +
                ",\ninstrument constructors: " + instrumentConstructorsEnabled +
                ",\ninstrument lambdas: " + instrumentLambdasEnabled +
                ",\nrecording policy: " + startRecordingPolicyPropertyValue +
                ",\nrecord collections: " + collectionsRecordingMode +
                ",\ntimestamps enabled: " + timestampsEnabled +
                ",\ntype validation enabled: " + typeValidationEnabled +
                ",\ntypesToPrintWithToString(TBD)=" + typesToPrint;
    }
}
