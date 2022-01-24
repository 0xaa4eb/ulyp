package com.ulyp.core;

import com.ulyp.transport.BinaryProcessMetadataDecoder;
import com.ulyp.transport.BinaryProcessMetadataEncoder;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class ProcessMetadata {

    public static final int WIRE_ID = 20;

    private final String mainClassName;
    private final String classPath;
    private final long pid;

    public void serialize(BinaryProcessMetadataEncoder encoder) {
        encoder.mainClassName(mainClassName);
        encoder.classPath(classPath);
        encoder.pid(pid);
    }

    public static ProcessMetadata deserialize(BinaryProcessMetadataDecoder decoder) {
        return ProcessMetadata.builder()
                .mainClassName(decoder.mainClassName())
                .classPath(decoder.classPath())
                .pid(decoder.pid())
                .build();
    }

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
