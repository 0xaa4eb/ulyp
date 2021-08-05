package com.ulyp.agent;

import com.ulyp.core.log.LogLevel;
import com.ulyp.core.log.LoggingSettings;
import com.ulyp.core.printers.CollectionPrinter;
import com.ulyp.core.printers.MapPrinter;
import com.ulyp.core.printers.ObjectBinaryPrinterType;
import com.ulyp.core.printers.ToStringPrinter;
import com.ulyp.core.process.ProcessInfo;
import com.ulyp.core.util.ClassMatcher;
import com.ulyp.core.util.ClassUtils;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.core.util.PackageList;
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

        if (AgentContext.isLoaded()) {
            return;
        }
        AgentContext.load();

        String logLevel = LoggingSettings.LOG_LEVEL.name();
        AgentContext instance = AgentContext.getInstance();

        Settings settings = Settings.fromSystemProperties();

        PackageList instrumentedPackages = settings.getInstrumentatedPackages();
        PackageList excludedPackages = settings.getExcludedFromInstrumentationPackages();
        RecordMethodList recordMethodList = settings.getRecordMethodList();

        if (recordMethodList == null || recordMethodList.isEmpty()) {
            // if not specified, then record main(String[] args) method as it's the only entry point to the program we have
            ProcessInfo processInfo = instance.getProcessInfo();
            recordMethodList = RecordMethodList.of(
                    new MethodMatcher(ClassMatcher.parse(ClassUtils.getSimpleNameFromName(processInfo.getMainClassName())), "main")
            );
        }

        System.out.println(ULYP_LOGO);
        System.out.println("ULYP agent started, logging level = " + logLevel + ", settings: " + settings);

        CollectionPrinter printer = (CollectionPrinter) ObjectBinaryPrinterType.COLLECTION_DEBUG_PRINTER.getInstance();
        printer.setMode(settings.getCollectionsRecordingMode());

        MapPrinter mapPrinter = (MapPrinter) ObjectBinaryPrinterType.MAP_PRINTER.getInstance();
        mapPrinter.setMode(settings.getCollectionsRecordingMode());

        ToStringPrinter toStringPrinter = (ToStringPrinter) (ObjectBinaryPrinterType.TO_STRING_PRINTER.getInstance());
        toStringPrinter.addClassNamesSupportPrinting(settings.getClassesToPrintWithToString());

        ElementMatcher.Junction<TypeDescription> tracingMatcher = null;

        for (String packageToInstrument : instrumentedPackages) {
            if (tracingMatcher == null) {
                tracingMatcher = ElementMatchers.nameStartsWith(packageToInstrument);
            } else {
                tracingMatcher = tracingMatcher.or(ElementMatchers.nameStartsWith(packageToInstrument));
            }
        }

        excludedPackages.add("java");
        excludedPackages.add("javax");
        excludedPackages.add("jdk");
        excludedPackages.add("sun");

        for (String excludedPackage : excludedPackages) {
            if (tracingMatcher == null) {
                tracingMatcher = ElementMatchers.not(ElementMatchers.nameStartsWith(excludedPackage));
            } else {
                tracingMatcher = tracingMatcher.and(ElementMatchers.not(ElementMatchers.nameStartsWith(excludedPackage)));
            }
        }

        ElementMatcher.Junction<TypeDescription> finalMatcher = ElementMatchers
                .not(ElementMatchers.nameStartsWith("com.ulyp"))
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("shadowed")));

        if (tracingMatcher != null) {
            finalMatcher = finalMatcher.and(tracingMatcher);
        }

        MethodFactory methodFactory = new MethodFactory(recordMethodList);

        AgentBuilder.Identified.Extendable agentBuilder = new AgentBuilder.Default()
                .type(finalMatcher)
                .transform((builder, typeDescription, classLoader, module) -> builder.visit(
                        Advice.withCustomMapping()
                                .bind(methodFactory)
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
                            .bind(methodFactory)
                            .to(ConstructorCallRecordingAdvice.class)
                            .on(ElementMatchers.isConstructor())
            ));
        }

        AgentBuilder agent = agentBuilder.with(AgentBuilder.TypeStrategy.Default.REDEFINE);
        // .with(AgentBuilder.LambdaInstrumentationStrategy.ENABLED);

        if (LoggingSettings.LOG_LEVEL == LogLevel.TRACE) {
            agent = agent.with(AgentBuilder.Listener.StreamWriting.toSystemOut());
        }
        agent.installOn(instrumentation);
    }
}
