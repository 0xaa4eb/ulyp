package com.ulyp.agent.options;

import com.ulyp.core.util.*;
import com.ulyp.agent.policy.AlwaysEnabledRecordingPolicy;
import com.ulyp.agent.policy.OverridableRecordingPolicy;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Agent options which define what packages to instrument, at which method recording should start, etc.
 * It's only possible to set settings via JMV system properties at the time.
 */
@Getter
public class AgentOptions {

    public static final boolean TIMESTAMPS_ENABLED;

    public static final String PACKAGES_PROPERTY = "ulyp.packages";
    public static final String START_RECORDING_POLICY_PROPERTY = "ulyp.policy";
    public static final String BIND_NETWORK_ADDRESS = "ulyp.bind";
    public static final String EXCLUDE_PACKAGES_PROPERTY = "ulyp.exclude-packages";
    public static final String EXCLUDE_CLASSES_PROPERTY = "ulyp.exclude-classes";
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

    private final AgentOption<String> recordingDataFilePath = new AgentOption<>(
            FILE_PATH_PROPERTY,
            text -> text,
            "Path to the file where recording data should be written"
    );
    private final AgentOption<List<String>> instrumentedPackages = new AgentOption<>(
            PACKAGES_PROPERTY,
            Collections.emptyList(),
            new ListParser<>(text -> text),
            ""
    );
    private final AgentOption<List<String>> excludedFromInstrumentationPackages = new AgentOption<>(
            EXCLUDE_PACKAGES_PROPERTY,
            Collections.emptyList(),
            new ListParser<>(text -> text),
            ""
    );;
    private final AgentOption<MethodMatcher> startRecordingMethodMatcher = new AgentOption<>(
            START_RECORDING_METHODS_PROPERTY,
            new MethodMatchersParser(),
            "Method at which start recording"
    );
    private final AgentOption<List<TypeMatcher>> excludeFromInstrumentationClasses = new AgentOption<>(
            EXCLUDE_CLASSES_PROPERTY,
            Collections.emptyList(),
            new ListParser<>(new TypeMatcherParser()),
            "Specifies a comma separated list of type matchers, which should be printed via toString() in the process of recording. " +
                    "Type matcher consists of ANT package class matcher and class name matcher. Examples are: " +
                    "1) org.springframework.** - all classes in org.springframework package.\n" +
                    "2) **.Command - classes of any package which have either Command class name or inherit/implement any class of such name.\n" +
                    "3) com.springframework.*.Command - combines both package and name matchers."
    );
    private final AgentOption<Boolean> instrumentConstructorsOption = new AgentOption<>(
            INSTRUMENT_CONSTRUCTORS_PROPERTY,
            false,
            new FlagParser(),
            "Indicates whether constructors should be instrumented (and possibly recorded). Correct values: 'true', 'false'. Defaults to 'false'"
    );
    private final AgentOption<Boolean> instrumentLambdasOption = new AgentOption<>(
            INSTRUMENT_LAMBDAS_PROPERTY,
            false,
            new FlagParser(),
            "Indicates whether lambdas should be instrumented (and possibly recorded). Correct values: 'true', 'false'. Defaults to 'false'"
    );
    private final AgentOption<Boolean> instrumentTypeInitializers = new AgentOption<>(
            INSTRUMENT_TYPE_INITIALIZERS,
            false,
            new FlagParser(),
            "(Experimental) Indicates whether type initializers (static blocks) should be instrumented (and possibly recorded). Correct values: 'true', 'false'. Empty is considered as 'false'"
    );
    private final AgentOption<OverridableRecordingPolicy> startRecordingPolicy = new AgentOption<>(
            START_RECORDING_POLICY_PROPERTY,
            new OverridableRecordingPolicy(new AlwaysEnabledRecordingPolicy()),
            new RecordingPolicyParser(),
            "The policy property which defines when any recording can start. " +
                    "If not set, then recording can start any time. " +
                    "Value 'delay:X' allows to set delay after which recording can start. X is specified in seconds. For example, 'delay:60'. " +
                    "Value 'api' makes the agent behaviour controllable through remote Grpc API."
    );
    private final AgentOption<CollectionsRecordingMode> collectionsRecordingMode = new AgentOption<>(
            RECORD_COLLECTIONS_PROPERTY,
            CollectionsRecordingMode::valueOf,
            "Defines if collections, maps and arrays should be recorded. Defaults to 'NONE' which allows the agent to pass all objects by reference" +
                    " to the background thread. 'JAVA' enables recording of Java standard library collections, maps and arrays. 'ALL' " +
                    "will record all collections (event 3rd party library collections) which might be very unpleasant, so use with care."
    );
    private final AgentOption<List<TypeMatcher>> typesToPrint = new AgentOption<>(
            PRINT_TYPES_PROPERTY,
            Collections.emptyList(),
            new ListParser<>(new TypeMatcherParser()),
            "Specifies a comma separated list of type matchers, which should be printed via toString() in the process of recording. " +
                    "Type matcher consists of ANT package class matcher and class name matcher. Examples are: " +
                    "1) org.springframework.** - all classes in org.springframework package.\n" +
                    "2) **.Command - classes of any package which have either Command class name or inherit/implement any class of such name.\n" +
                    "3) com.springframework.*.Command - combines both package and name matchers."
    );
    private final AgentOption<String> bindNetworkAddress = new AgentOption<>(
            AGENT_DISABLED_PROPERTY,
            text -> text,
            "Network address at which GRPC API should bind"
    );
    private final AgentOption<Boolean> agentDisabled = new AgentOption<>(
            AGENT_DISABLED_PROPERTY,
            false,
            new FlagParser(),
            "Allows to disable the agent altogether via single property."
    );
    private final AgentOption<Boolean> timestampsEnabled = new AgentOption<>(
            TIMESTAMPS_ENABLED_PROPERTY,
            false,
            new FlagParser(),
            "Records timestamps spent in each method in nanoseconds. Correct values: 'true', 'false'. Defaults to 'false'"
    );
    private final AgentOption<Boolean> metricsEnabled = new AgentOption<>(
            METRICS_ENABLED_PROPERTY,
            false,
            new FlagParser(),
            "(Experimental) Indicates if metrics are enabled. Metrics are dumped to stderr periodically. Correct values: 'true', 'false'. Defaults to 'false'"
    );
    private final AgentOption<Boolean> typeValidationEnabled = new AgentOption<>(
            TYPE_VALIDATION_ENABLED_PROPERTY,
            false,
            new FlagParser(),
            "Byte-buddy type validation flag. Correct values: 'true', 'false'. Defaults to 'false'"
    );

    @Nullable
    public String getBindNetworkAddress() {
        return bindNetworkAddress.get();
    }

    @NotNull
    public MethodMatcher getRecordMethodList() {
        MethodMatcher methodMatcher = startRecordingMethodMatcher.get();
        if (methodMatcher != null) {
            return methodMatcher;
        } else {
            return new SingleMethodMatcher(TypeMatcher.parse(ProcessMetadata.getMainClassNameFromProp()), "main");
        }
    }

    @Override
    public String toString() {
        return "file: " + recordingDataFilePath +
                ",\npackages to instrument: " + instrumentedPackages +
                ",\npackages excluded from instrumentation: " + excludedFromInstrumentationPackages +
                ",\nrecording will start at methods: " + startRecordingMethodMatcher +
                ",\ninstrument constructors: " + instrumentConstructorsOption +
                ",\ninstrument lambdas: " + instrumentLambdasOption +
                ",\nrecord collections: " + collectionsRecordingMode +
                ",\ntimestamps enabled: " + timestampsEnabled +
                ",\ntype validation enabled: " + typeValidationEnabled +
                ",\ntypesToPrintWithToString(TBD)=" + typesToPrint;
    }

    public boolean isAgentEnabled() {
        return !agentDisabled.get();
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

    public boolean isTypeValidationEnabled() {
        return typeValidationEnabled.get();
    }
}
