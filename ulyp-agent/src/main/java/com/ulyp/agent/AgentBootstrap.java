package com.ulyp.agent;

import com.ulyp.agent.util.StreamDrainer;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

/**
 * Takes ulyp-agent-classes.jar added as a file into the ulyp-agent.jar and extracts it into
 * temporary jar file.
 * <p>
 * It then appends ulyp-agent-classes.jar to bootstrap loader search. This is necessary because some classes are
 * defined by custom class loaders which should be later recorded by the agent.
 */
public class AgentBootstrap {

    private static final String AGENT_CLASSES_JAR_RESOURCE_NAME = "ulyp-agent-classes.jarr";
    private static final String ULYP_TMP_DIR_PROPERTY = "ulyp.tmp-dir";
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

        Path tmpJarFile;
        try {
            String tmpDir = System.getProperty(ULYP_TMP_DIR_PROPERTY);
            if (tmpDir != null) {
                tmpJarFile = Files.createTempFile(Paths.get(tmpDir), "ulyp-agent-classes", ".jar");
            } else {
                tmpJarFile = Files.createTempFile("ulyp-agent-classes", ".jar");
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create tmp file", e);
        }

        try (
                InputStream inputStream = thisClass.getClassLoader().getResourceAsStream(AGENT_CLASSES_JAR_RESOURCE_NAME);
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpJarFile.toFile(), false))
        ) {
            outputStream.write(new StreamDrainer().drain(inputStream));
            File classesJar = tmpJarFile.toFile();
            System.out.println("Unpacking ulyp-agent-classes jar file to " + classesJar);
            classesJar.deleteOnExit();
            return classesJar;
        } catch (IOException e) {
            throw new RuntimeException("Could not copy classpath resource " + AGENT_CLASSES_JAR_RESOURCE_NAME, e);
        }
    }
}

