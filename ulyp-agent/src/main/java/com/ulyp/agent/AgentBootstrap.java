package com.ulyp.agent;

import com.ulyp.agent.util.StreamDrainer;
import com.ulyp.agent.util.Version;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.jar.JarFile;

/**
 * Takes ulyp-agent-core.jar added as a file into the ulyp-agent.jar and extracts it into
 * temporary jar file.
 * <p>
 * It then appends ulyp-agent-core.jar to bootstrap loader search. This is necessary because some classes are
 * defined by custom class loaders which should be later recorded by the agent.
 */
public class AgentBootstrap {

    private static final String AGENT_CLASSES_JAR_INTERNAL_RESOURCE_NAME = "ulyp-agent-core.jarrr";
    private static final String ULYP_TMP_DIR_PROPERTY = "ulyp.tmp-dir";
    private static final Class<?> thisClass = AgentBootstrap.class;

    public static void premain(String args, Instrumentation instrumentation) {
        try {
            instrumentation.appendToBootstrapClassLoaderSearch(
                    new JarFile(copyJarToTmp())
            );
        } catch (IOException e) {
            throw new AgentInitializationException(e);
        }

        Agent.start(args, instrumentation);
    }

    private static File copyJarToTmp() {

        File tmpJarFile;
        try {
            String tmpDir = System.getProperty(ULYP_TMP_DIR_PROPERTY);
            String fileName = "ulyp-agent-core-" + Version.get() + "-" + Version.getBuildTimeEpochMilli() + ".jar";
            if (tmpDir != null) {
                tmpJarFile = Paths.get(tmpDir, fileName).toFile();
            } else {
                tmpJarFile = Paths.get(System.getProperty("java.io.tmpdir"), fileName).toFile();
            }

            if (tmpJarFile.exists() && tmpJarFile.length() == 0L) {
                if (!tmpJarFile.delete()) {
                    if (tmpDir != null) {
                        tmpJarFile = Files.createTempFile(Paths.get(tmpDir), "ulyp-agent-core", ".jar").toFile();
                    } else {
                        tmpJarFile = Files.createTempFile("ulyp-agent-core", ".jar").toFile();
                    }
                }
            }
        } catch (IOException e) {
            throw new AgentInitializationException("Error ocurred while bootstrapping agent", e);
        }

        int internalJarSize;
        try (InputStream inputStream = thisClass.getClassLoader().getResourceAsStream(AGENT_CLASSES_JAR_INTERNAL_RESOURCE_NAME)) {
            if (inputStream == null) {
                throw new AgentInitializationException("Could not find " + AGENT_CLASSES_JAR_INTERNAL_RESOURCE_NAME + " in ulyp jar");
            }
            internalJarSize = new StreamDrainer().getAvailableBytesCount(inputStream);
        } catch (IOException e) {
            throw new AgentInitializationException("Error while checking size of internal jar file", e);
        }

        if (tmpJarFile.exists() && tmpJarFile.length() == internalJarSize) {
            return tmpJarFile;
        }

        try (
                InputStream inputStream = thisClass.getClassLoader().getResourceAsStream(AGENT_CLASSES_JAR_INTERNAL_RESOURCE_NAME);
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpJarFile, false))
        ) {
            outputStream.write(new StreamDrainer().drain(inputStream));
            File classesJar = tmpJarFile;
            System.out.println("Unpacking ulyp-agent-core jar file to " + classesJar);
            classesJar.deleteOnExit();
            return classesJar;
        } catch (IOException e) {
            throw new AgentInitializationException("Could not copy classpath resource " + AGENT_CLASSES_JAR_INTERNAL_RESOURCE_NAME, e);
        }
    }
}

