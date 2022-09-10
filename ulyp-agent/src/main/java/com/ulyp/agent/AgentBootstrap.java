package com.ulyp.agent;

import com.ulyp.agent.util.StreamDrainer;
import com.ulyp.agent.util.Version;

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

    private static final String AGENT_CLASSES_JAR_INTERNAL_RESOURCE_NAME = "ulyp-agent-classes.jarr";
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

        File tmpJarFile;
        try {
            String tmpDir = System.getProperty(ULYP_TMP_DIR_PROPERTY);
            String fileName = "ulyp-agent-classes-" + Version.get() + "-" + Version.getBuildTimeEpochMilli() + ".jar";
            if (tmpDir != null) {
                tmpJarFile = Paths.get(tmpDir, fileName).toFile();
            } else {
                tmpJarFile = Paths.get(System.getProperty("java.io.tmpdir"), fileName).toFile();
            }

            if (tmpJarFile.exists() && tmpJarFile.length() == 0L) {
                if (!tmpJarFile.delete()) {
                    if (tmpDir != null) {
                        tmpJarFile = Files.createTempFile(Paths.get(tmpDir), "ulyp-agent-classes", ".jar").toFile();
                    } else {
                        tmpJarFile = Files.createTempFile("ulyp-agent-classes", ".jar").toFile();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error ocurred while bootstrapping agent", e);
        }

        if (tmpJarFile.exists() && tmpJarFile.length() > 0) {
            return tmpJarFile;
        }

        try (
                InputStream inputStream = thisClass.getClassLoader().getResourceAsStream(AGENT_CLASSES_JAR_INTERNAL_RESOURCE_NAME);
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpJarFile, false))
        ) {
            outputStream.write(new StreamDrainer().drain(inputStream));
            File classesJar = tmpJarFile;
            System.out.println("Unpacking ulyp-agent-classes jar file to " + classesJar);
            classesJar.deleteOnExit();
            return classesJar;
        } catch (IOException e) {
            throw new RuntimeException("Could not copy classpath resource " + AGENT_CLASSES_JAR_INTERNAL_RESOURCE_NAME, e);
        }
    }
}

