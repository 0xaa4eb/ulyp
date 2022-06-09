package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Records instances of {@link File} class
 */
public class FileRecorder extends ObjectRecorder {

    private static final String FILE_TYPE_NAME = File.class.getName();

    protected FileRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.getName().equals(FILE_TYPE_NAME);
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new FileRecord(objectType, input.readString());
    }

    @Override
    public void write(Object object, @NotNull Type classDescription, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        out.writeString(((File) object).getPath());
    }
}
