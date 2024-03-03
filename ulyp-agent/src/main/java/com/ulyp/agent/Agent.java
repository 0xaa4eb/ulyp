package com.ulyp.agent;

import java.lang.instrument.Instrumentation;
import java.util.Optional;

import com.ulyp.agent.advice.*;
import com.ulyp.agent.util.ByteBuddyMethodResolver;
import com.ulyp.agent.util.ByteBuddyTypeConverter;
import com.ulyp.agent.util.ErrorLoggingInstrumentationListener;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.PrintingRecorder;
import com.ulyp.core.recorders.collections.CollectionRecorder;
import com.ulyp.core.recorders.collections.MapRecorder;
import com.ulyp.core.util.TypeMatcher;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.util.PackageList;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

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

        Settings settings = Settings.fromSystemProperties();
        if (!settings.isAgentEnabled()) {
            System.out.println("ULYP agent disabled, no code will be instrumented");
            return;
        }

        // Touch first and initialize shadowed slf4j
        String logLevel = LoggingSettings.getLoggingLevel();

        if (AgentContext.isLoaded()) {
            return;
        } else {
            AgentContext.init();
        }
        AgentContext context = AgentContext.getCtx();
        StartRecordingMethods startRecordingMethods = settings.getRecordMethodList();

        System.out.println(ULYP_LOGO);
        System.out.println("ULYP agent started, logging level = " + logLevel + ", settings: " + settings);

        CollectionRecorder recorder = (CollectionRecorder) ObjectRecorderRegistry.COLLECTION_RECORDER.getInstance();
        recorder.setMode(settings.getCollectionsRecordingMode());

        MapRecorder mapRecorder = (MapRecorder) ObjectRecorderRegistry.MAP_RECORDER.getInstance();
        mapRecorder.setMode(settings.getCollectionsRecordingMode());

        PrintingRecorder toStringRecorder = (PrintingRecorder) (ObjectRecorderRegistry.TO_STRING_RECORDER.getInstance());
        toStringRecorder.addClassesToPrint(settings.getTypesToPrint());

        ElementMatcher.Junction<TypeDescription> ignoreMatcher = buildIgnoreMatcher(settings);
        ElementMatcher.Junction<TypeDescription> instrumentationMatcher = buildInstrumentationMatcher(settings);

        MethodIdFactory methodIdFactory = new MethodIdFactory(context.getMethodRepository());

        AsmVisitorWrapper.ForDeclaredMethods startRecordingMethodAdvice = Advice.withCustomMapping()
                .bind(methodIdFactory)
                .to(StartRecordingMethodAdvice.class)
                .on(buildStartRecordingMethodsMatcher(settings));
        AsmVisitorWrapper.ForDeclaredMethods methodCallAdvice = Advice.withCustomMapping()
                .bind(methodIdFactory)
                .to(MethodAdvice.class)
                .on(buildContinueRecordingMethodsMatcher(settings));
/*        AsmVisitorWrapper.ForDeclaredMethods methodCallAdvice0Args = Advice.withCustomMapping()
                .bind(methodIdFactory)
                .to(MethodAdvice0Args.class)
                .on(buildContinueRecordingMethodsMatcher(settings).and(ElementMatchers.takesNoArguments()));*/
        AsmVisitorWrapper.ForDeclaredMethods startRecordingConstructorAdvice = Advice.withCustomMapping()
                .bind(methodIdFactory)
                .to(StartRecordingConstructorAdvice.class)
                .on(buildStartRecordingConstructorMatcher(settings));
        AsmVisitorWrapper.ForDeclaredMethods constructorAdvice = Advice.withCustomMapping()
                .bind(methodIdFactory)
                .to(ConstructorAdvice.class)
                .on(buildContinueRecordingConstructorMatcher(settings));

        AgentBuilder.Identified.Extendable agentBuilder = new AgentBuilder.Default()
            .ignore(ignoreMatcher)
            .type(instrumentationMatcher)
            .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {
                DynamicType.Builder<?> visitor = builder.visit(startRecordingMethodAdvice).visit(methodCallAdvice);
                if (settings.instrumentConstructors()) {
                    visitor = visitor.visit(startRecordingConstructorAdvice).visit(constructorAdvice);
                }
                return visitor;
            });

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

    // TODO reduce convertions to domain model
    private static ElementMatcher.Junction<MethodDescription> buildStartRecordingConstructorMatcher(Settings settings) {
        return ElementMatchers.isConstructor().and(
                methodDescription -> settings.getRecordMethodList().shouldStartRecording(ByteBuddyMethodResolver.INSTANCE.resolve(methodDescription))
        );
    }

    private static ElementMatcher.Junction<MethodDescription> buildContinueRecordingConstructorMatcher(Settings settings) {
        return ElementMatchers.isConstructor().and(
                methodDescription -> !settings.getRecordMethodList().shouldStartRecording(ByteBuddyMethodResolver.INSTANCE.resolve(methodDescription))
        );
    }

    private static ElementMatcher.Junction<MethodDescription> buildStartRecordingMethodsMatcher(Settings settings) {
        return basicMethodsMatcher(settings).and(
                methodDescription -> settings.getRecordMethodList().shouldStartRecording(ByteBuddyMethodResolver.INSTANCE.resolve(methodDescription))
        );
    }

    private static ElementMatcher.Junction<MethodDescription> buildContinueRecordingMethodsMatcher(Settings settings) {
        return basicMethodsMatcher(settings).and(
                methodDescription -> !settings.getRecordMethodList().shouldStartRecording(ByteBuddyMethodResolver.INSTANCE.resolve(methodDescription))
        );
    }

    private static ElementMatcher.Junction<MethodDescription> basicMethodsMatcher(Settings settings) {
        ElementMatcher.Junction<MethodDescription> methodMatcher = ElementMatchers.isMethod()
            .and(ElementMatchers.not(ElementMatchers.isAbstract()))
            .and(ElementMatchers.not(ElementMatchers.isConstructor()));

        if (settings.instrumentTypeInitializers()) {
            return methodMatcher.or(ElementMatchers.isTypeInitializer());
        } else {
            return methodMatcher;
        }
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

        ElementMatcher.Junction<TypeDescription> instrumentationMatcher = buildInstrumentationMatcher(settings);
        if (instrumentationMatcher != ElementMatchers.<TypeDescription>any()) {
            ignoreMatcher = ElementMatchers.not(instrumentationMatcher).and(ignoreMatcher);
        }

        for (String excludedPackage : excludedPackages) {
            ignoreMatcher = ignoreMatcher.or(ElementMatchers.nameStartsWith(excludedPackage));
        }

        for (TypeMatcher excludeTypeMatcher : settings.getExcludeFromInstrumentationClasses()) {
            ByteBuddyTypeConverter typeConverter = ByteBuddyTypeConverter.SUPER_TYPE_DERIVING_INSTANCE;
            ignoreMatcher = ignoreMatcher.or(
                target -> excludeTypeMatcher.matches(typeConverter.convert(target.asGenericType()))
            );
        }

        return ignoreMatcher;
    }
}
