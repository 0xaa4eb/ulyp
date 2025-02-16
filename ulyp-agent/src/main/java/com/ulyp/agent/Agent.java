package com.ulyp.agent;

import com.ulyp.agent.advice.*;
import com.ulyp.agent.options.AgentOptions;
import com.ulyp.agent.util.ByteBuddyMethodConverter;
import com.ulyp.agent.util.ByteBuddyTypeConverter;
import com.ulyp.agent.util.InstrumentationListener;
import com.ulyp.core.Converter;
import com.ulyp.core.Method;
import com.ulyp.core.Type;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.util.TypeMatcher;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.util.List;
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

        AgentOptions options = new AgentOptions();
        if (!options.isAgentEnabled()) {
            System.out.println("ULYP agent disabled, no code will be instrumented");
            return;
        }

        // Touch first and initialize shadowed slf4j
        String logLevel = LoggingSettings.getLoggingLevel();

        if (AgentContext.isLoaded()) {
            return;
        } else {
            ByteBuddyTypeConverter typeConverter = new ByteBuddyTypeConverter();

            AgentContextBootstrap bootstrap = AgentContextBootstrap.builder()
                    .typeConverter(typeConverter)
                    .methodConverter(new ByteBuddyMethodConverter(typeConverter))
                    .build();

            AgentContext.init(bootstrap);
        }
        AgentContext context = AgentContext.getCtx();

        System.out.println(ULYP_LOGO);
        System.out.println("ULYP agent started, logging level = " + logLevel + ", settings: " + options);

        ElementMatcher.Junction<TypeDescription> ignoreMatcher = buildIgnoreMatcher(options, context.getTypeConverter());
        ElementMatcher.Junction<TypeDescription> instrumentationMatcher = buildInstrumentationMatcher(options);

        MethodIdFactory methodIdFactory = new MethodIdFactory(context.getMethodRepository(), context.getMethodResolver());

        AsmVisitorWrapper.ForDeclaredMethods startRecordingMethodAdvice = Advice.withCustomMapping()
                .bind(methodIdFactory)
                .to(StartRecordingMethodAdvice.class)
                .on(buildStartRecordingMethodsMatcher(options, context.getMethodResolver()));
        AsmVisitorWrapper.ForDeclaredMethods methodCallAdvice = Advice.withCustomMapping()
                .bind(methodIdFactory)
                .to(MethodAdvice.class)
                .on(buildContinueRecordingMethodsMatcher(options, context.getMethodResolver()).and(x -> x.getParameters().size() > 3));
        AsmVisitorWrapper.ForDeclaredMethods methodCallAdviceNoParams = Advice.withCustomMapping()
                .bind(methodIdFactory)
                .to(MethodAdviceNoArgs.class)
                .on(buildContinueRecordingMethodsMatcher(options, context.getMethodResolver()).and(x -> x.getParameters().isEmpty()));
        AsmVisitorWrapper.ForDeclaredMethods methodCallAdviceOneParams = Advice.withCustomMapping()
                .bind(methodIdFactory)
                .to(MethodAdviceOneArg.class)
                .on(buildContinueRecordingMethodsMatcher(options, context.getMethodResolver()).and(x -> x.getParameters().size() == 1));
        AsmVisitorWrapper.ForDeclaredMethods methodCallAdviceTwoParams = Advice.withCustomMapping()
                .bind(methodIdFactory)
                .to(MethodAdviceTwoArgs.class)
                .on(buildContinueRecordingMethodsMatcher(options, context.getMethodResolver()).and(x -> x.getParameters().size() == 2));
        AsmVisitorWrapper.ForDeclaredMethods methodCallAdviceThreeParams = Advice.withCustomMapping()
                .bind(methodIdFactory)
                .to(MethodAdviceThreeArgs.class)
                .on(buildContinueRecordingMethodsMatcher(options, context.getMethodResolver()).and(x -> x.getParameters().size() == 3));

        TypeValidation typeValidation = options.isTypeValidationEnabled() ? TypeValidation.ENABLED : TypeValidation.DISABLED;

        AgentBuilder.Identified.Extendable agentBuilder = new AgentBuilder.Default(new ByteBuddy().with(typeValidation))
            .ignore(ignoreMatcher)
            .type(instrumentationMatcher)
            .transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder
                    .visit(methodCallAdviceNoParams)
                    .visit(methodCallAdviceOneParams)
                    .visit(methodCallAdviceTwoParams)
                    .visit(methodCallAdviceThreeParams)
                    .visit(startRecordingMethodAdvice)
                    .visit(methodCallAdvice)
            );

        if (options.isInstrumentConstructorsEnabled()) {
            AsmVisitorWrapper.ForDeclaredMethods startRecordingConstructorAdvice = Advice.withCustomMapping()
                    .bind(methodIdFactory)
                    .to(StartRecordingConstructorAdvice.class)
                    .on(buildStartRecordingConstructorMatcher(options, context.getMethodResolver()));
            AsmVisitorWrapper.ForDeclaredMethods constructorAdvice = Advice.withCustomMapping()
                    .bind(methodIdFactory)
                    .to(ConstructorAdvice.class)
                    .on(buildContinueRecordingConstructorMatcher(options, context.getMethodResolver()));

            agentBuilder = agentBuilder.transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                    builder.visit(startRecordingConstructorAdvice).visit(constructorAdvice));
        }

        AgentBuilder agent = agentBuilder.with(AgentBuilder.TypeStrategy.Default.REDEFINE);
        if (options.isInstrumentLambdasEnabled()) {
            agent = agent.with(AgentBuilder.LambdaInstrumentationStrategy.ENABLED);
        }

        agent = agent.with(new InstrumentationListener());

        agent.installOn(instrumentation);
    }

    private static ElementMatcher.Junction<MethodDescription> buildStartRecordingConstructorMatcher(
            AgentOptions options,
            Converter<MethodDescription, Method> methodResolver) {
        return ElementMatchers.isConstructor().and(
                methodDescription -> options.getRecordMethodList().matches(methodResolver.convert(methodDescription))
        );
    }

    private static ElementMatcher.Junction<MethodDescription> buildContinueRecordingConstructorMatcher(
            AgentOptions options,
            Converter<MethodDescription, Method> methodResolver) {
        return ElementMatchers.isConstructor().and(
                methodDescription -> !options.getRecordMethodList().matches(methodResolver.convert(methodDescription))
        );
    }

    private static ElementMatcher.Junction<MethodDescription> buildStartRecordingMethodsMatcher(
            AgentOptions options,
            Converter<MethodDescription, Method> methodResolver) {
        return basicMethodsMatcher(options).and(
                methodDescription -> options.getRecordMethodList().matches(methodResolver.convert(methodDescription))
        );
    }

    private static ElementMatcher.Junction<MethodDescription> buildContinueRecordingMethodsMatcher(
            AgentOptions options,
            Converter<MethodDescription, Method> methodResolver) {
        return basicMethodsMatcher(options).and(
                methodDescription -> !options.getRecordMethodList().matches(methodResolver.convert(methodDescription))
        );
    }

    private static ElementMatcher.Junction<MethodDescription> basicMethodsMatcher(AgentOptions options) {
        ElementMatcher.Junction<MethodDescription> methodMatcher = ElementMatchers.isMethod()
                .and(ElementMatchers.not(ElementMatchers.isAbstract()))
                .and(ElementMatchers.not(ElementMatchers.isConstructor()));

        if (options.isInstrumentTypeInitializersEnabled()) {
            return methodMatcher.or(ElementMatchers.isTypeInitializer());
        } else {
            return methodMatcher;
        }
    }

    private static ElementMatcher.Junction<TypeDescription> buildInstrumentationMatcher(AgentOptions options) {
        List<String> instrumentedPackages = options.getInstrumentedPackages().get();
        ElementMatcher.Junction<TypeDescription> instrumentationMatcher = null;

        for (String packageToInstrument : instrumentedPackages) {
            if (instrumentationMatcher == null) {
                instrumentationMatcher = ElementMatchers.nameStartsWith(packageToInstrument);
            } else {
                instrumentationMatcher = instrumentationMatcher.or(ElementMatchers.nameStartsWith(packageToInstrument));
            }
        }

        return Optional.ofNullable(instrumentationMatcher).orElse(ElementMatchers.any());
    }

    private static ElementMatcher.Junction<TypeDescription> buildIgnoreMatcher(
            AgentOptions options,
            Converter<TypeDescription.Generic, Type> typeConverter) {
        List<String> excludedPackages = options.getExcludedFromInstrumentationPackages().get();

        ElementMatcher.Junction<TypeDescription> ignoreMatcher = ElementMatchers.nameStartsWith("java.")
            .or(ElementMatchers.nameStartsWith("javax."))
            .or(ElementMatchers.nameStartsWith("jdk."))
            .or(ElementMatchers.nameStartsWith("sun"))
            .or(ElementMatchers.nameStartsWith("shadowed"))
            .or(ElementMatchers.nameStartsWith("com.sun"))
            .or(ElementMatchers.nameStartsWith("com.ulyp"));

        ElementMatcher.Junction<TypeDescription> instrumentationMatcher = buildInstrumentationMatcher(options);
        if (instrumentationMatcher != ElementMatchers.<TypeDescription>any()) {
            ignoreMatcher = ElementMatchers.not(instrumentationMatcher).and(ignoreMatcher);
        }

        for (String excludedPackage : excludedPackages) {
            ignoreMatcher = ignoreMatcher.or(ElementMatchers.nameStartsWith(excludedPackage));
        }

        for (TypeMatcher excludeTypeMatcher : options.getExcludeFromInstrumentationClasses().get()) {
            ignoreMatcher = ignoreMatcher.or(
                target -> excludeTypeMatcher.matches(typeConverter.convert(target.asGenericType()))
            );
        }

        return ignoreMatcher;
    }
}
