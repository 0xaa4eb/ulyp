package com.ulyp.core.process;

public class ProcessInfo {

    private final String mainClassName;
    private final Classpath classpath;

    public ProcessInfo() {
        this.mainClassName = getMainClassNameFromProp();
        this.classpath = new Classpath();
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public Classpath getClasspath() {
        return classpath;
    }

    /**
     * @return main class name of this java process or null if it's not possible to define it
     */
    private static String getMainClassNameFromProp() {
        String mainFromProp = System.getProperty("sun.java.command");
        if (mainFromProp != null && !mainFromProp.isEmpty()) {
            int space = mainFromProp.indexOf(' ');
            if (space > 0) {
                return mainFromProp.substring(0, space);
            } else {
                return mainFromProp;
            }
        } else {
            return "Unknown";
        }
    }
}
