package com.ulyp.agent;

import com.ulyp.agent.util.StreamDrainer;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes ulyp-agent-classes.jar added as a file into the ulyp-agent.jar and extracts it into
 * temporary jar file.
 *
 * It then appends ulyp-agent-classes.jar to bootstrap loader search. This is necessary for user-defined
 * class laoders to deal with ulyp
 */
public class AgentBootstrap {

    private static final String AGENT_CLASSES_JAR_RESOURCE_NAME = "ulyp-agent-classes.jarr";
    private static final Class<?> thisClass = AgentBootstrap.class;

    public static void premain(String args, Instrumentation instrumentation) {
        try {

            instrumentation.appendToBootstrapClassLoaderSearch(
                    new JarFile(copyJarToTmp())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Agent.start(args, instrumentation);
    }

    private static File copyJarToTmp() {

        Path tmpJarFile = null;
        try {
            tmpJarFile = Files.createTempFile("ulyp-agent-classes", ".jar");
        } catch (IOException e) {
            throw new RuntimeException("Could not create tmp file", e);
        }

        try (
                InputStream inputStream = thisClass.getClassLoader().getResourceAsStream(AGENT_CLASSES_JAR_RESOURCE_NAME);
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpJarFile.toFile(), false))
        ) {
            outputStream.write(new StreamDrainer().drain(inputStream));
            File f = tmpJarFile.toFile();
            System.out.println("**** " + f);
            f.deleteOnExit();
            return f;
        } catch (IOException e) {
            throw new RuntimeException("Could not copy classpath resource " + AGENT_CLASSES_JAR_RESOURCE_NAME, e);
        }
    }
}

