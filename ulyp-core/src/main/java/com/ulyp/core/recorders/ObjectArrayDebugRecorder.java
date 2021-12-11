package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.BinaryOutputAppender;

import java.util.ArrayList;
import java.util.List;

public class ObjectArrayDebugRecorder extends ObjectRecorder {

    protected ObjectArrayDebugRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        // used manually by ObjectArrayPrinter
        return type.isNonPrimitveArray();
    }

    @Override
    public ObjectRecord read(Type type, BinaryInput input, ByIdTypeResolver typeResolver) {
        int arrayLength = input.readInt();
        List<ObjectRecord> items = new ArrayList<>();
        int recordedItemsCount = input.readInt();
        for (int i = 0; i < recordedItemsCount; i++) {
            items.add(input.readObject(typeResolver));
        }
        return new ObjectArrayRecord(
                type,
                arrayLength,
                items
        );
    }

    @Override
    public void write(Object object, Type classDescription, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {
            Object[] array = (Object[]) object;
            int length = array.length;
            appender.append(length);
            int itemsToRecord = Math.min(3, length);
            appender.append(itemsToRecord);

            for (int i = 0; i < itemsToRecord; i++) {
                appender.append(array[i], typeResolver);
            }
        }
    }
}