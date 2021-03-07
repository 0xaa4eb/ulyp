package com.ulyp.core.printers;

import com.ulyp.core.DecodingContext;
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
    public ObjectRepresentation read(TypeInfo type, BinaryInput input, DecodingContext decodingContext) {
        int arrayLength = input.readInt();
        List<ObjectRepresentation> items = new ArrayList<>();
        int recordedItemsCount = input.readInt();
        for (int i = 0; i < recordedItemsCount; i++) {
            TypeInfo itemClassTypeInfo = decodingContext.getType(input.readInt());
            ObjectBinaryPrinter printer = ObjectBinaryPrinterType.printerForId(input.readByte());
            items.add(printer.read(itemClassTypeInfo, input, decodingContext));
        }
        return new ObjectArrayRepresentation(
                type,
                arrayLength,
                items
        );
    }

    @Override
    public void write(Object object, TypeInfo classDescription, BinaryOutput out, TypeResolver runtime) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {
            Object[] array = (Object[]) object;
            int length = array.length;
            appender.append(length);
            int itemsToRecord = Math.min(3, length);
            appender.append(itemsToRecord);
            for (int i = 0; i < itemsToRecord; i++) {
                Object item = array[i];
                TypeInfo itemType = runtime.get(item);
                appender.append(itemType.getId());
                ObjectBinaryPrinter printer = item != null ? itemType.getSuggestedPrinter() : ObjectBinaryPrinterType.NULL_PRINTER.getInstance();
                appender.append(printer.getId());
                printer.write(item, itemType, appender, runtime);
            }
        }
    }
}
