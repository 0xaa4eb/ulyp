package com.ulyp.core.serializers;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;

public class ProcessMetadataSerializer implements Serializer<ProcessMetadata> {

    public static final ProcessMetadataSerializer instance = new ProcessMetadataSerializer();

    @Override
    public ProcessMetadata deserialize(BinaryInput input) {
        return ProcessMetadata.builder()
                .pid(input.readLong())
                .mainClassName(input.readString())
                .build();
    }

    @Override
    public void serialize(BinaryOutput out, ProcessMetadata object) {
        out.write(object.getPid());
        out.write(object.getMainClassName());
    }
}
