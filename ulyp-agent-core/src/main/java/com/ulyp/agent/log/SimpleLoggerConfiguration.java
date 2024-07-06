package com.ulyp.agent.log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.slf4j.helpers.Util;

/**
 * Forked from slf4j-simple
 */
public class SimpleLoggerConfiguration {

    private static final String CONFIGURATION_FILE = "simplelogger.properties";

    private static final boolean SHOW_THREAD_NAME_DEFAULT = true;
    private static final boolean SHOW_DATE_TIME_DEFAULT = false;
    private static final String DATE_TIME_FORMAT_STR_DEFAULT = null;
    private static String dateTimeFormatStr = DATE_TIME_FORMAT_STR_DEFAULT;
    private static int DEFAULT_LOG_LEVEL_DEFAULT = SimpleLogger.LOG_LEVEL_INFO;
    private final static boolean SHOW_LOG_NAME_DEFAULT = true;
    private static final boolean SHOW_SHORT_LOG_NAME_DEFAULT = false;
    private static final boolean LEVEL_IN_BRACKETS_DEFAULT = false;
    private static String LOG_FILE_DEFAULT = "System.err";
    private static final boolean CACHE_OUTPUT_STREAM_DEFAULT = false;
    private static final String WARN_LEVELS_STRING_DEFAULT = "WARN";

    int defaultLogLevel = DEFAULT_LOG_LEVEL_DEFAULT;
    boolean showDateTime = SHOW_DATE_TIME_DEFAULT;
    DateFormat dateFormatter = null;
    boolean showThreadName = SHOW_THREAD_NAME_DEFAULT;
    boolean showLogName = SHOW_LOG_NAME_DEFAULT;
    boolean showShortLogName = SHOW_SHORT_LOG_NAME_DEFAULT;
    boolean levelInBrackets = LEVEL_IN_BRACKETS_DEFAULT;
    private String logFile = LOG_FILE_DEFAULT;
    OutputChoice outputChoice = null;
    String warnLevelString = WARN_LEVELS_STRING_DEFAULT;

    private boolean cacheOutputStream = CACHE_OUTPUT_STREAM_DEFAULT;
    private final Properties properties = new Properties();

    void init() {
        loadProperties();

        String defaultLogLevelString = getStringProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, null);
        if (defaultLogLevelString != null)
            defaultLogLevel = stringToLevel(defaultLogLevelString);

        showLogName = getBooleanProperty(SimpleLogger.SHOW_LOG_NAME_KEY, SimpleLoggerConfiguration.SHOW_LOG_NAME_DEFAULT);
        showShortLogName = getBooleanProperty(SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, SHOW_SHORT_LOG_NAME_DEFAULT);
        showDateTime = getBooleanProperty(SimpleLogger.SHOW_DATE_TIME_KEY, SHOW_DATE_TIME_DEFAULT);
        showThreadName = getBooleanProperty(SimpleLogger.SHOW_THREAD_NAME_KEY, SHOW_THREAD_NAME_DEFAULT);
        dateTimeFormatStr = getStringProperty(SimpleLogger.DATE_TIME_FORMAT_KEY, DATE_TIME_FORMAT_STR_DEFAULT);
        levelInBrackets = getBooleanProperty(SimpleLogger.LEVEL_IN_BRACKETS_KEY, LEVEL_IN_BRACKETS_DEFAULT);
        warnLevelString = getStringProperty(SimpleLogger.WARN_LEVEL_STRING_KEY, WARN_LEVELS_STRING_DEFAULT);

        logFile = getStringProperty(SimpleLogger.LOG_FILE_KEY, logFile);

        cacheOutputStream = getBooleanProperty(SimpleLogger.CACHE_OUTPUT_STREAM_STRING_KEY, CACHE_OUTPUT_STREAM_DEFAULT);
        outputChoice = computeOutputChoice(logFile, cacheOutputStream);

        if (dateTimeFormatStr != null) {
            try {
                dateFormatter = new SimpleDateFormat(dateTimeFormatStr);
            } catch (IllegalArgumentException e) {
                Util.report("Bad date format in " + CONFIGURATION_FILE + "; will output relative time", e);
            }
        }
    }

    private void loadProperties() {
        // Add props from the resource simplelogger.properties
        InputStream in = AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
            public InputStream run() {
                ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
                if (threadCL != null) {
                    return threadCL.getResourceAsStream(CONFIGURATION_FILE);
                } else {
                    return ClassLoader.getSystemResourceAsStream(CONFIGURATION_FILE);
                }
            }
        });
        if (null != in) {
            try {
                properties.load(in);
            } catch (java.io.IOException e) {
                // ignored
            } finally {
                try {
                    in.close();
                } catch (java.io.IOException e) {
                    // ignored
                }
            }
        }
    }

    String getStringProperty(String name, String defaultValue) {
        String prop = getStringProperty(name);
        return (prop == null) ? defaultValue : prop;
    }

    boolean getBooleanProperty(String name, boolean defaultValue) {
        String prop = getStringProperty(name);
        return (prop == null) ? defaultValue : "true".equalsIgnoreCase(prop);
    }

    String getStringProperty(String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (SecurityException e) {
            // Ignore
        }
        return (prop == null) ? properties.getProperty(name) : prop;
    }

    static int stringToLevel(String levelStr) {
        if ("trace".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_TRACE;
        } else if ("debug".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_DEBUG;
        } else if ("info".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_INFO;
        } else if ("warn".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_WARN;
        } else if ("error".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_ERROR;
        } else if ("off".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_OFF;
        }
        // assume INFO by default
        return SimpleLogger.LOG_LEVEL_INFO;
    }

    private static OutputChoice computeOutputChoice(String logFile, boolean cacheOutputStream) {
        if ("System.err".equalsIgnoreCase(logFile))
            if (cacheOutputStream)
                return new OutputChoice(OutputChoice.OutputChoiceType.CACHED_SYS_ERR);
            else
                return new OutputChoice(OutputChoice.OutputChoiceType.SYS_ERR);
        else if ("System.out".equalsIgnoreCase(logFile)) {
            if (cacheOutputStream)
                return new OutputChoice(OutputChoice.OutputChoiceType.CACHED_SYS_OUT);
            else
                return new OutputChoice(OutputChoice.OutputChoiceType.SYS_OUT);
        } else {
            try {
                FileOutputStream fos = new FileOutputStream(logFile);
                PrintStream printStream = new PrintStream(fos);
                return new OutputChoice(printStream);
            } catch (FileNotFoundException e) {
                Util.report("Could not open [" + logFile + "]. Defaulting to System.err", e);
                return new OutputChoice(OutputChoice.OutputChoiceType.SYS_ERR);
            }
        }
    }

}
