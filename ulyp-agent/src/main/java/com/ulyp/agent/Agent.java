package com.ulyp.agent;

import com.ulyp.agent.util.ByteBuddyTypeResolver;
import com.ulyp.agent.util.ErrorLoggingInstrumentationListener;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.recorders.collections.CollectionRecorder;
import com.ulyp.core.recorders.collections.MapRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.ToStringPrintingRecorder;
import com.ulyp.core.util.ClassMatcher;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.core.util.PackageList;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.util.Optional;

/**
 * The agent entry point which is invoked by JVM itself
 */
public class Agent {

    private static final String ULYP_LOGO =
        "   __  __    __ __  __    ____ \n" +
            "  / / / /   / / \\ \\/ /   / __ \\\n" +
            " / / / /   / /   \\  /   / /_/ /\n" +
            "/ /_/ /   / /___ / /   / ____/ \n" +
            "\\____/   /_____//_/   /_/      \n" +
            "                               ";

    public static void start(String args, Instrumentation instrumentation) {

        // Touch first and initialize shadowed slf4j
        String logLevel = LoggingSettings.getLoggingLevel();

        if (AgentContext.isLoaded()) {
            return;
        } else {
            AgentContext.init(ByteBuddyTypeResolver.getInstance());
        }

        AgentContext instance = AgentContext.getInstance();

        Settings settings = Settings.fromSystemProperties();
        if (settings.isAgentDisabled()) {
            System.out.println("ULYP agent disabled, no code will be instrumented");
            return;
        }

        StartRecordingMethods startRecordingMethods = settings.getRecordMethodList();

        if (startRecordingMethods.isEmpty()) {
            startRecordingMethods = StartRecordingMethods.of(
                new MethodMatcher(ClassMatcher.parse(ProcessMetadata.getMainClassNameFromProp()), "main")
            );
        }

        System.out.println(ULYP_LOGO);
        System.out.println("ULYP agent started, logging level = " + logLevel + ", settings: " + settings);

        CollectionRecorder recorder = (CollectionRecorder) ObjectRecorderRegistry.COLLECTION_RECORDER.getInstance();
        recorder.setMode(settings.getCollectionsRecordingMode());

        MapRecorder mapRecorder = (MapRecorder) ObjectRecorderRegistry.MAP_RECORDER.getInstance();
        mapRecorder.setMode(settings.getCollectionsRecordingMode());

        ToStringPrintingRecorder toStringRecorder = (ToStringPrintingRecorder) (ObjectRecorderRegistry.TO_STRING_RECORDER.getInstance());
        toStringRecorder.addClassesToPrint(settings.getClassesToPrint());

        ElementMatcher.Junction<TypeDescription> ignoreMatcher = buildIgnoreMatcher(settings);
        ElementMatcher.Junction<TypeDescription> instrumentationMatcher = buildInstrumentationMatcher(settings);

        MethodIdFactory methodIdFactory = new MethodIdFactory(startRecordingMethods);

        AgentBuilder.Identified.Extendable agentBuilder = new AgentBuilder.Default()
            .ignore(ignoreMatcher)
            .type(instrumentationMatcher)
            .transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder.visit(
                Advice.withCustomMapping()
                    .bind(methodIdFactory)
                    .to(MethodCallRecordingAdvice.class)
                    .on(ElementMatchers
                        .isMethod()
                        .and(ElementMatchers.not(ElementMatchers.isAbstract()))
                        .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                    )
            ));

        if (settings.instrumentConstructors()) {
            agentBuilder = agentBuilder.transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder.visit(
                Advice.withCustomMapping()
                    .bind(methodIdFactory)
                    .to(ConstructorCallRecordingAdvice.class)
                    .on(ElementMatchers.isConstructor())
            ));
        }

        AgentBuilder agent = agentBuilder.with(AgentBuilder.TypeStrategy.Default.REDEFINE);
        if (settings.instrumentLambdas()) {
            agent = agent.with(AgentBuilder.LambdaInstrumentationStrategy.ENABLED);
        }

        if (LoggingSettings.TRACE_ENABLED) {
            agent = agent.with(AgentBuilder.Listener.StreamWriting.toSystemOut());
        } else {
            agent = agent.with(new ErrorLoggingInstrumentationListener());
        }

        agent.installOn(instrumentation);
    }

    private static ElementMatcher.Junction<TypeDescription> buildInstrumentationMatcher(Settings settings) {
        PackageList instrumentatedPackages = settings.getInstrumentatedPackages();
        ElementMatcher.Junction<TypeDescription> instrumentationMatcher = null;

        for (String packageToInstrument : instrumentatedPackages) {
            if (instrumentationMatcher == null) {
                instrumentationMatcher = ElementMatchers.nameStartsWith(packageToInstrument);
            } else {
                instrumentationMatcher = instrumentationMatcher.or(ElementMatchers.nameStartsWith(packageToInstrument));
            }
        }

        return Optional.ofNullable(instrumentationMatcher).orElse(ElementMatchers.any());
    }

    private static ElementMatcher.Junction<TypeDescription> buildIgnoreMatcher(Settings settings) {
        PackageList excludedPackages = settings.getExcludedFromInstrumentationPackages();

        ElementMatcher.Junction<TypeDescription> ignoreMatcher = ElementMatchers.nameStartsWith("java.")
            .or(ElementMatchers.nameStartsWith("javax."))
            .or(ElementMatchers.nameStartsWith("jdk."))
            .or(ElementMatchers.nameStartsWith("sun"))
            .or(ElementMatchers.nameStartsWith("shadowed"))
            .or(ElementMatchers.nameStartsWith("com.sun"))
            .or(ElementMatchers.nameStartsWith("com.ulyp"));

        for (String excludedPackage : excludedPackages) {
            ignoreMatcher = ignoreMatcher.or(ElementMatchers.nameStartsWith(excludedPackage));
        }

        for (ClassMatcher excludeClassMatcher : settings.getExcludeFromInstrumentationClasses()) {
            ignoreMatcher = ignoreMatcher.or(
                target -> excludeClassMatcher.matches(ByteBuddyTypeResolver.getInstance().resolve(target.asGenericType()))
            );
        }

        return ignoreMatcher;
    }
}
