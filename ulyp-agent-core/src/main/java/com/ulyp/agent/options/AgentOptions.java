package com.ulyp.agent.options;

import com.ulyp.agent.StartRecordingMethods;
import com.ulyp.agent.policy.OverridableRecordingPolicy;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.core.util.TypeMatcher;
import com.ulyp.core.util.CommaSeparatedList;
import com.ulyp.core.util.PackageList;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Agent options which define what packages to instrument, at which method recording should start, etc.
 * It's only possible to set settings via JMV system properties at the time.
 */
@Getter
@Builder
public class AgentOptions {

    public static final boolean TIMESTAMPS_ENABLED;

    public static final String PACKAGES_PROPERTY = "ulyp.packages";
    public static final String START_RECORDING_POLICY_PROPERTY = "ulyp.policy";
    public static final String BIND_NETWORK_ADDRESS = "ulyp.bind";
    public static final String EXCLUDE_PACKAGES_PROPERTY = "ulyp.exclude-packages";
    public static final String EXCLUDE_CLASSES_PROPERTY = "ulyp.exclude-classes";
    public static final String EXCLUDE_RECORDING_METHODS_PROPERTY = "ulyp.exclude-methods";
    public static final String START_RECORDING_METHODS_PROPERTY = "ulyp.methods";
    public static final String PRINT_TYPES_PROPERTY = "ulyp.print";
    public static final String FILE_PATH_PROPERTY = "ulyp.file";
    public static final String INSTRUMENT_CONSTRUCTORS_PROPERTY = "ulyp.constructors";
    public static final String INSTRUMENT_LAMBDAS_PROPERTY = "ulyp.lambdas";
    public static final String INSTRUMENT_TYPE_INITIALIZERS = "ulyp.type-initializers";
    public static final String RECORD_COLLECTIONS_PROPERTY = "ulyp.collections";
    public static final String TIMESTAMPS_ENABLED_PROPERTY = "ulyp.timestamps";
    public static final String TYPE_VALIDATION_ENABLED_PROPERTY = "ulyp.type-validation";
    public static final String AGENT_DISABLED_PROPERTY = "ulyp.off";
    public static final String METRICS_ENABLED_PROPERTY = "ulyp.metrics";

    static {
        // make 'static final'. bytecode will be thrown off if the feature is disabled
        TIMESTAMPS_ENABLED = System.getProperty(TIMESTAMPS_ENABLED_PROPERTY) != null;
    }

    @Builder.Default
    private final AgentOption<String> recordingDataFilePath = new AgentOption<>(
            FILE_PATH_PROPERTY,
            text -> text,
            "Path to the file where recording data should be written"
    );
    private final PackageList instrumentatedPackages;
    private final PackageList excludedFromInstrumentationPackages;
    private final StartRecordingMethods startRecordingMethods;
    private final List<TypeMatcher> excludeFromInstrumentationClasses;
    @Builder.Default
    private final AgentOption<Boolean> instrumentConstructorsOption = new AgentOption<>(
            INSTRUMENT_CONSTRUCTORS_PROPERTY,
            new FlagParser(),
            "Indicates whether constructors should be instrumented (and possibly recorded). Correct values: 'true', 'false'. Empty is considered as 'true'"
    );
    @Builder.Default
    private final AgentOption<Boolean> instrumentLambdasOption = new AgentOption<>(
            INSTRUMENT_LAMBDAS_PROPERTY,
            new FlagParser(),
            "Indicates whether lambdas should be instrumented (and possibly recorded). Correct values: 'true', 'false'. Empty is considered as 'true'"
    );
    @Builder.Default
    private final AgentOption<Boolean> instrumentTypeInitializers = new AgentOption<>(
            INSTRUMENT_TYPE_INITIALIZERS,
            new FlagParser(),
            "(Experimental) Indicates whether type initializer blocks should be instrumented (and possibly recorded). Correct values: 'true', 'false'. Empty is considered as 'true'"
    );
    @Builder.Default
    private final AgentOption<OverridableRecordingPolicy> startRecordingPolicy = new AgentOption<>(
            START_RECORDING_POLICY_PROPERTY,
            new RecordingPolicyParser(),
            "The policy property which defines when any recording can start. " +
                    "If not set, then recording can start any time. " +
                    "Value 'delay:X' allows to set delay after which recording can start. X is specified in seconds. For example, 'delay:60'. " +
                    "Value 'api' makes the agent behaviour controllable through remote Grpc API."
    );
    @Builder.Default
    private final AgentOption<CollectionsRecordingMode> collectionsRecordingMode = new AgentOption<>(
            RECORD_COLLECTIONS_PROPERTY,
            CollectionsRecordingMode::valueOf,
            "Defines if collections, maps and arrays should be recorded. Defaults to 'NONE' which allows the agent to pass all objects by reference" +
                    " to the background thread. 'JAVA' enables recording of Java standard library collections, maps and arrays. 'ALL' " +
                    "will record all collections (event 3rd party library collections) which might be very unpleasant, so use with care."
    );
    private final Set<TypeMatcher> typesToPrint;
    private final String bindNetworkAddress;
    private final boolean agentEnabled;
    private final boolean timestampsEnabled;
    private final boolean metricsEnabled;
    private final boolean typeValidationEnabled;

    public static AgentOptions fromSystemProperties() {
        String bindNetworkAddress = System.getProperty(BIND_NETWORK_ADDRESS);

        PackageList instrumentationPackages = new PackageList(CommaSeparatedList.parse(System.getProperty(PACKAGES_PROPERTY, "")));
        PackageList excludedPackages = new PackageList(CommaSeparatedList.parse(System.getProperty(EXCLUDE_PACKAGES_PROPERTY, "")));
        List<TypeMatcher> excludeClassesFromInstrumentation = CommaSeparatedList.parse(System.getProperty(EXCLUDE_CLASSES_PROPERTY, ""))
                .stream()
                .map(TypeMatcher::parse)
                .collect(Collectors.toList());

        String methodsToRecordRaw = System.getProperty(START_RECORDING_METHODS_PROPERTY, "");
        String excludeMethodsToRecordRaw = System.getProperty(EXCLUDE_RECORDING_METHODS_PROPERTY, "");
        StartRecordingMethods startRecordingMethods = StartRecordingMethods.parse(methodsToRecordRaw, excludeMethodsToRecordRaw);
        if (startRecordingMethods.isEmpty()) {
            startRecordingMethods = StartRecordingMethods.of(
                    new MethodMatcher(TypeMatcher.parse(ProcessMetadata.getMainClassNameFromProp()), "main")
            );
        }

        String recordingDataFilePath = System.getProperty(FILE_PATH_PROPERTY);
        if (recordingDataFilePath == null) {
            throw new RuntimeException("Property " + FILE_PATH_PROPERTY + " must be set");
        }

        boolean timestampsEnabled = System.getProperty(TIMESTAMPS_ENABLED_PROPERTY) != null;

        boolean agentEnabled = System.getProperty(AGENT_DISABLED_PROPERTY) == null;
        boolean metricsEnabled = System.getProperty(METRICS_ENABLED_PROPERTY) != null;
        boolean typeValidationEnabled = System.getProperty(TYPE_VALIDATION_ENABLED_PROPERTY) != null;

        Set<TypeMatcher> typesToPrint =
                CommaSeparatedList.parse(System.getProperty(PRINT_TYPES_PROPERTY, ""))
                        .stream()
                        .map(TypeMatcher::parse)
                        .collect(Collectors.toSet());

        return AgentOptions.builder()
                .instrumentatedPackages(instrumentationPackages)
                .excludedFromInstrumentationPackages(excludedPackages)
                .startRecordingMethods(startRecordingMethods)
                .typesToPrint(typesToPrint)
                .excludeFromInstrumentationClasses(excludeClassesFromInstrumentation)
                .bindNetworkAddress(bindNetworkAddress)
                .agentEnabled(agentEnabled)
                .timestampsEnabled(timestampsEnabled)
                .metricsEnabled(metricsEnabled)
                .typeValidationEnabled(typeValidationEnabled)
                .build();
    }

    @Nullable
    public String getBindNetworkAddress() {
        return bindNetworkAddress;
    }

    @NotNull
    public StartRecordingMethods getRecordMethodList() {
        return startRecordingMethods;
    }

    @Override
    public String toString() {
        return "file: " + recordingDataFilePath +
                ",\npackages to instrument: " + instrumentatedPackages +
                ",\npackages excluded from instrumentation: " + excludedFromInstrumentationPackages +
                ",\nstart recording at methods: " + startRecordingMethods +
                ",\ninstrument constructors: " + instrumentConstructorsOption +
                ",\ninstrument lambdas: " + instrumentLambdasOption +
                ",\nrecord collections: " + collectionsRecordingMode +
                ",\ntimestamps enabled: " + timestampsEnabled +
                ",\ntype validation enabled: " + typeValidationEnabled +
                ",\ntypesToPrintWithToString(TBD)=" + typesToPrint;
    }

    public boolean isInstrumentConstructorsEnabled() {
        return instrumentConstructorsOption.get();
    }

    public boolean isInstrumentLambdasEnabled() {
        return instrumentLambdasOption.get();
    }

    public boolean isInstrumentTypeInitializersEnabled() {
        return instrumentTypeInitializers.get();
    }
}
