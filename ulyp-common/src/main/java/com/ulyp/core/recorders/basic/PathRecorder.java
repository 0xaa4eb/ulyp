package com.ulyp.core.recorders.basic;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecorder;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;


/**
 * Records instances of {@link java.nio.file.Path} class
 */
public class PathRecorder extends ObjectRecorder {

    public PathRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return Path.class.isAssignableFrom(type);
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public FileRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        return new FileRecord(objectType, input.readString());
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        out.write(((Path) object).toString());
    }
}
