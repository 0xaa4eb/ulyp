package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;
import com.ulyp.core.printers.bytes.BinaryOutputAppender;

import java.util.ArrayList;
import java.util.List;

public class ObjectArrayDebugPrinter extends ObjectBinaryPrinter {

    protected ObjectArrayDebugPrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(TypeInfo type) {
        // used manually by ObjectArrayPrinter
        return type.isNonPrimitveArray();
    }

    @Override
    public ObjectRepresentation read(TypeInfo type, BinaryInput input, ByIdTypeResolver typeResolver) {
        int arrayLength = input.readInt();
        List<ObjectRepresentation> items = new ArrayList<>();
        int recordedItemsCount = input.readInt();
        for (int i = 0; i < recordedItemsCount; i++) {
            items.add(input.readObject(typeResolver));
        }
        return new ObjectArrayRepresentation(
                type,
                arrayLength,
                items
        );
    }

    @Override
    public void write(Object object, TypeInfo classDescription, BinaryOutput out, TypeResolver typeResolver) throws Exception {
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
