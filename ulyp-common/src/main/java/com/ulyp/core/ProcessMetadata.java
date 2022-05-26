package com.ulyp.core;

import com.ulyp.transport.BinaryProcessMetadataDecoder;
import com.ulyp.transport.BinaryProcessMetadataEncoder;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@ToString
public class ProcessMetadata {

    public static final int WIRE_ID = 20;

    private final String mainClassName;
    private final List<String> classPathFiles;
    private final long pid;

    public static ProcessMetadata deserialize(BinaryProcessMetadataDecoder decoder) {
        List<String> classPathFiles = new ArrayList<>();
        decoder.classPathFiles().forEachRemaining(val -> classPathFiles.add(val.value()));

        return ProcessMetadata.builder()
                .mainClassName(decoder.mainClassName())
                .classPathFiles(classPathFiles)
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

    public void serialize(BinaryProcessMetadataEncoder encoder) {
        encoder.pid(pid);
        BinaryProcessMetadataEncoder.ClassPathFilesEncoder classPathFilesEncoder = encoder.classPathFilesCount(classPathFiles.size());
        for (String val : classPathFiles) {
            classPathFilesEncoder.next().value(val);
        }
        encoder.mainClassName(mainClassName);
    }
}
