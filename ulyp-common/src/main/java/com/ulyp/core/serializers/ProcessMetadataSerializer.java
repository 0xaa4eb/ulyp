package com.ulyp.core.serializers;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;

public class ProcessMetadataSerializer implements Serializer<ProcessMetadata> {

    public static final ProcessMetadataSerializer instance = new ProcessMetadataSerializer();

    @Override
    public ProcessMetadata deserialize(BytesIn input) {
        return ProcessMetadata.builder()
                .pid(input.readLong())
                .mainClassName(input.readString())
                .build();
    }

    @Override
    public void serialize(BytesOut out, ProcessMetadata object) {
        out.write(object.getPid());
        out.write(object.getMainClassName());
    }
}
