package com.ulyp.core;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class ProcessMetadata {

    public static final int WIRE_ID = 20;

    private final String mainClassName;
    private final long pid;

    public static String getMainClassNameFromProp() {
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
