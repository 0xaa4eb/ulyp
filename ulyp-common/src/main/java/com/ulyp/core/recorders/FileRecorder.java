package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Records instances of {@link File} class
 */
public class FileRecorder extends ObjectRecorder {

    protected FileRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == File.class;
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        return new FileRecord(objectType, input.readString());
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        out.write(((File) object).getPath());
    }
}
