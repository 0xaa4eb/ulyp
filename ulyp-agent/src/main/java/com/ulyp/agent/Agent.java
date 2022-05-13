package com.ulyp.agent;

import com.ulyp.agent.util.ByteBuddyTypeResolver;
import com.ulyp.agent.util.ErrorLoggingInstrumentationListener;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.recorders.CollectionRecorder;
import com.ulyp.core.recorders.MapRecorder;
import com.ulyp.core.recorders.ObjectRecorderType;
import com.ulyp.core.recorders.ToStringRecorder;
import com.ulyp.core.util.*;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

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
        }
        AgentContext.setLoaded();

        AgentContext instance = AgentContext.getInstance();

        Settings settings = Settings.fromSystemProperties();
        if (settings.isAgentDisabled()) {
            System.out.println("ULYP agent disabled, no code will be instrumented");
            return;
        }

        PackageList instrumentedPackages = settings.getInstrumentatedPackages();
        PackageList excludedPackages = settings.getExcludedFromInstrumentationPackages();
        RecordMethodList recordMethodList = settings.getRecordMethodList();

        if (recordMethodList == null || recordMethodList.isEmpty()) {
            recordMethodList = RecordMethodList.of(
                    new MethodMatcher(ClassMatcher.parse(ProcessMetadata.getMainClassNameFromProp()), "main")
            );
        }

        System.out.println(ULYP_LOGO);
        System.out.println("ULYP agent started, logging level = " + logLevel + ", settings: " + settings);

        CollectionRecorder recorder = (CollectionRecorder) ObjectRecorderType.COLLECTION_RECORDER.getInstance();
        recorder.setMode(settings.getCollectionsRecordingMode());

        MapRecorder mapRecorder = (MapRecorder) ObjectRecorderType.MAP_RECORDER.getInstance();
        mapRecorder.setMode(settings.getCollectionsRecordingMode());

        ToStringRecorder toStringRecorder = (ToStringRecorder) (ObjectRecorderType.TO_STRING_RECORDER.getInstance());
        toStringRecorder.addClassesToPrint(settings.getClassesToPrint());

        ElementMatcher.Junction<TypeDescription> instrumentationMatcher = null;

        for (String packageToInstrument : instrumentedPackages) {
            if (instrumentationMatcher == null) {
                instrumentationMatcher = ElementMatchers.nameStartsWith(packageToInstrument);
            } else {
                instrumentationMatcher = instrumentationMatcher.or(ElementMatchers.nameStartsWith(packageToInstrument));
            }
        }

        excludedPackages.add("java");
        excludedPackages.add("javax");
        excludedPackages.add("jdk");
        excludedPackages.add("sun");

        for (String excludedPackage : excludedPackages) {
            if (instrumentationMatcher == null) {
                instrumentationMatcher = ElementMatchers.not(ElementMatchers.nameStartsWith(excludedPackage));
            } else {
                instrumentationMatcher = instrumentationMatcher.and(ElementMatchers.not(ElementMatchers.nameStartsWith(excludedPackage)));
            }
        }

        for (ClassMatcher excludeClassMatcher : settings.getExcludeFromInstrumentationClasses()) {
            instrumentationMatcher = instrumentationMatcher.and(
                    target -> !excludeClassMatcher.matches(ByteBuddyTypeResolver.getInstance().resolve(target.asGenericType()))
            );
        }

        ElementMatcher.Junction<TypeDescription> finalMatcher = ElementMatchers
                .not(ElementMatchers.nameStartsWith("com.ulyp"))
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("shadowed")));

        if (instrumentationMatcher != null) {
            finalMatcher = finalMatcher.and(instrumentationMatcher);
        }

        MethodIdFactory methodIdFactory = new MethodIdFactory(recordMethodList);

        AgentBuilder.Identified.Extendable agentBuilder = new AgentBuilder.Default()
                .type(finalMatcher)
                .transform((builder, typeDescription, classLoader, module) -> builder.visit(
                        Advice.withCustomMapping()
                                .bind(methodIdFactory)
                                .to(MethodCallRecordingAdvice.class)
                                .on(ElementMatchers
                                        .isMethod()
                                        .and(ElementMatchers.not(ElementMatchers.isAbstract()))
                                        .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                                )
                ));

        if (settings.shouldRecordConstructors()) {
            agentBuilder = agentBuilder.transform((builder, typeDescription, classLoader, module) -> builder.visit(
                    Advice.withCustomMapping()
                            .bind(methodIdFactory)
                            .to(ConstructorCallRecordingAdvice.class)
                            .on(ElementMatchers.isConstructor())
            ));
        }

        AgentBuilder agent = agentBuilder.with(AgentBuilder.TypeStrategy.Default.REDEFINE);
        // .with(AgentBuilder.LambdaInstrumentationStrategy.ENABLED);

        if (LoggingSettings.TRACE_ENABLED) {
            agent = agent.with(AgentBuilder.Listener.StreamWriting.toSystemOut());
        } else {
            agent = agent.with(new ErrorLoggingInstrumentationListener());
        }

        agent.installOn(instrumentation);
    }
}
