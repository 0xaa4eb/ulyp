package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;


/**
 * Records instances of {@link java.nio.file.Path} class
 */
public class PathRecorder extends ObjectRecorder {

    private static final String PATH_TYPE_NAME = java.nio.file.Path.class.getName();

    protected PathRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Type type) {
        return type.getName().equals(PATH_TYPE_NAME);
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new FileRecord(objectType, input.readString());
    }

    @Override
    public void write(Object object, @NotNull Type classDescription, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        out.writeString(((Path) object).toString());
    }
}
