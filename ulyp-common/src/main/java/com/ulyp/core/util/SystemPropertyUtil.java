package com.ulyp.core.util;

import lombok.extern.slf4j.Slf4j;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A collection of utility methods to retrieve and parse the values of the Java system properties.
 */
@Slf4j
public final class SystemPropertyUtil {

    public static boolean contains(String key) {
        return get(key) != null;
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(final String key, String def) {
        String value = null;
        try {
            if (System.getSecurityManager() == null) {
                value = System.getProperty(key);
            } else {
                value = AccessController.doPrivileged(new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return System.getProperty(key);
                    }
                });
            }
        } catch (SecurityException e) {
            log.warn("Unable to retrieve a system property '{}'; default values will be used.", key, e);
        }

        if (value == null) {
            return def;
        }

        return value;
    }

    public static int getInt(String key, int def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim();
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            // Ignore
        }

        log.warn("Unable to parse the integer system property '{}':{} - using the default value: {}", key, value, def);
        return def;
    }

    public static long getLong(String key, long def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim();
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            // Ignore
        }

        log.warn("Unable to parse the long integer system property '{}':{} - using the default value: {}", key, value, def);
        return def;
    }

    private SystemPropertyUtil() {
        // Unused
    }
}
