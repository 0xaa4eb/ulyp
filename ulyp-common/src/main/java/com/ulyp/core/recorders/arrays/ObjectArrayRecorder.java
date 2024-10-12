package com.ulyp.core.recorders.arrays;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ObjectArrayRecorder extends ObjectRecorder {

    @Setter
    private volatile boolean enabled = false;

    public ObjectArrayRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return enabled && Object[].class.isAssignableFrom(type);
    }

    @Override
    public ObjectRecord read(@NotNull Type type, BytesIn input, ByIdTypeResolver typeResolver) {
        int arrayLength = input.readVarInt();
        int recordedItemsCount = input.readVarInt();
        List<ObjectRecord> items = new ArrayList<>(recordedItemsCount);
        for (int i = 0; i < recordedItemsCount; i++) {
            items.add(input.readObject(typeResolver));
        }
        return new ObjectArrayRecord(type, arrayLength, items);
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        Object[] array = (Object[]) object;
        int length = array.length;
        out.writeVarInt(length);
        int itemsToRecord = Math.min(3, length);
        out.writeVarInt(itemsToRecord);

        for (int i = 0; i < itemsToRecord; i++) {
            out.write(array[i], typeResolver);
        }
    }
}
