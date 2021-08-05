package com.ulyp.core;

import com.ulyp.transport.BinaryProcessMetadataDecoder;
import com.ulyp.transport.BinaryProcessMetadataEncoder;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProcessMetadata {

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
}
