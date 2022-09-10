package com.ulyp.agent.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Version {

    private static final String PROPERTIES_FILENAME = "version.properties";

    public static String get() {
        return getBuildProperty("version");
    }

    public static long getBuildTimeEpochMilli() {
        return Long.parseLong(getBuildProperty("build_time"));
    }

    private static String getBuildProperty(String name) {
        try {
            InputStream propsFileInputStream = Version.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME);
            if (propsFileInputStream == null) {
                throw new RuntimeException(PROPERTIES_FILENAME + " file is not found in the agent jar file.");
            }

            Properties properties = new Properties();
            properties.load(propsFileInputStream);
            return (String) properties.get(name);
        } catch (IOException e) {
            throw new RuntimeException("Could not read from " + PROPERTIES_FILENAME + " file: " + e.getMessage(), e);
        }
    }
}
