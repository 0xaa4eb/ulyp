package com.ulyp.core.recorders.arrays;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;


/**
 * Char array recorder. It currently only records the length of array.
 */
@ThreadSafe
public class CharArrayRecorder extends ObjectRecorder {

    // Intentionally not volatile
    @Setter
    private boolean enabled = false;

    public CharArrayRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return enabled && type == char[].class;
    }

    @Override
    public CharArrayRecord read(@NotNull Type type, BytesIn input, ByIdTypeResolver typeResolver) {
        IdentityObjectRecord identity = (IdentityObjectRecord) ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().read(type, input, typeResolver);
        return new CharArrayRecord(type, identity, input.readVarInt());
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().write(object, out, typeResolver);
        char[] array = (char[]) object;
        out.writeVarInt(array.length);
    }
}
