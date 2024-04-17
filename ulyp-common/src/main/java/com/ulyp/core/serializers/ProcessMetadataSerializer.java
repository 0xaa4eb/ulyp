package com.ulyp.core.serializers;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;

import java.util.ArrayList;
import java.util.List;

public class ProcessMetadataSerializer implements Serializer<ProcessMetadata> {

    public static final ProcessMetadataSerializer instance = new ProcessMetadataSerializer();

    @Override
    public ProcessMetadata deserialize(BytesIn input) {
        long pid = input.readLong();
        String mainClassName = input.readString();
        int classPathJarFileCount = input.readInt();
        List<String> classpath = new ArrayList<>();
        for (int i = 0; i < classPathJarFileCount; i++) {
            classpath.add(input.readString());
        }

        return ProcessMetadata.builder()
                .pid(pid)
                .mainClassName(mainClassName)
                .classpath(classpath)
                .build();
    }

    @Override
    public void serialize(BytesOut out, ProcessMetadata object) {
        out.write(object.getPid());
        out.write(object.getMainClassName());

        List<String> classpath = object.getClasspath();
        if (classpath != null) {
            out.write(classpath.size());
            for (String jarFilePath : classpath) {
                out.write(jarFilePath);
            }
        } else {
            out.write(0);
        }
    }
}
