package com.ulyp.core.recorders.arrays;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;

@ThreadSafe
public class ObjectArrayRecorder extends ObjectRecorder {

    // Intentionally not volatile
    @Setter
    private boolean enabled = false;
    @Setter
    private int maxItemsToRecord;

    public ObjectArrayRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return enabled && Object[].class.isAssignableFrom(type);
    }

    @Override
    public ArrayRecord read(@NotNull Type type, BytesIn input, ByIdTypeResolver typeResolver) {
        int arrayLength = input.readVarInt();
        int recordedItemsCount = input.readVarInt();
        List<ObjectRecord> items = new ArrayList<>(recordedItemsCount);
        for (int i = 0; i < recordedItemsCount; i++) {
            items.add(input.readObject(typeResolver));
        }
        return new ArrayRecord(type, arrayLength, items);
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        Object[] array = (Object[]) object;
        int length = array.length;
        out.writeVarInt(length);
        int itemsToRecord = Math.min(maxItemsToRecord, length);
        out.writeVarInt(itemsToRecord);

        for (int i = 0; i < itemsToRecord; i++) {
            out.write(array[i], typeResolver);
        }
    }
}
