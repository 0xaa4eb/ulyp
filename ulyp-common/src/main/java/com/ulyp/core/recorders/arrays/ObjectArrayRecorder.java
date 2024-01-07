package com.ulyp.core.recorders.arrays;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.BufferBinaryOutput;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ObjectArrayRecorder extends ObjectRecorder {

    public ObjectArrayRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return Object[].class.isAssignableFrom(type);
    }

    @Override
    public ObjectRecord read(@NotNull Type type, BinaryInput input, ByIdTypeResolver typeResolver) {
        int arrayLength = input.readInt();
        List<ObjectRecord> items = new ArrayList<>();
        int recordedItemsCount = input.readInt();
        for (int i = 0; i < recordedItemsCount; i++) {
            items.add(input.readObject(typeResolver));
        }
        return new ObjectArrayRecord(type, arrayLength, items);
    }

    @Override
    public void write(Object object, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        try (BinaryOutput nestedOut = out.nest()) {
            Object[] array = (Object[]) object;
            int length = array.length;
            nestedOut.append(length);
            int itemsToRecord = Math.min(3, length);
            nestedOut.append(itemsToRecord);

            for (int i = 0; i < itemsToRecord; i++) {
                nestedOut.append(array[i], typeResolver);
            }
        }
    }
}
