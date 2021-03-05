package com.ulyp.core.printers;

import com.ulyp.core.AgentRuntime;
import com.ulyp.core.DecodingContext;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;
import com.ulyp.core.printers.bytes.BinaryOutputAppender;
import com.ulyp.core.printers.bytes.Checkpoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CollectionPrinter extends ObjectBinaryPrinter {

    private volatile boolean active = true;
    private CollectionsRecordingMode mode;

    public static final int MAX_ITEMS_TO_RECORD = 3;

    private static final int RECORDED_ITEMS = 1;
    private static final int RECORDED_IDENTITY_ONLY = 0;

    protected CollectionPrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(TypeInfo type) {
        return mode.supports(type) && type.isCollection();
    }

    public void setMode(CollectionsRecordingMode mode) {
        this.mode = mode;
    }

    @Override
    public ObjectRepresentation read(TypeInfo classDescription, BinaryInput input, DecodingContext decodingContext) {
        int recordedItems = input.readInt();

        if (recordedItems == RECORDED_ITEMS) {
            int collectionSize = input.readInt();
            List<ObjectRepresentation> items = new ArrayList<>();
            int recordedItemsCount = input.readInt();
            for (int i = 0; i < recordedItemsCount; i++) {
                TypeInfo itemClassTypeInfo = decodingContext.getType(input.readInt());
                ObjectBinaryPrinter printer = ObjectBinaryPrinterType.printerForId(input.readByte());
                items.add(printer.read(itemClassTypeInfo, input, decodingContext));
            }
            return new CollectionRepresentation(
                    classDescription,
                    collectionSize,
                    items
            );
        } else {
            return ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance().read(classDescription, input, decodingContext);
        }
    }

    @Override
    public void write(Object object, TypeInfo classDescription, BinaryOutput out, AgentRuntime runtime) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {

            if (active) {
                appender.append(RECORDED_ITEMS);
                Checkpoint checkpoint = appender.checkpoint();
                try {
                    Collection<?> collection = (Collection<?>) object;
                    int length = collection.size();
                    appender.append(length);
                    int itemsToRecord = Math.min(MAX_ITEMS_TO_RECORD, length);
                    appender.append(itemsToRecord);
                    Iterator<?> iterator = collection.iterator();
                    int recorded = 0;

                    while (recorded < itemsToRecord && iterator.hasNext()) {
                        Object item = iterator.next();
                        TypeInfo itemType = runtime.get(item);
                        appender.append(itemType.getId());
                        ObjectBinaryPrinter printer = item != null ? itemType.getSuggestedPrinter() : ObjectBinaryPrinterType.NULL_PRINTER.getInstance();
                        appender.append(printer.getId());
                        printer.write(item, itemType, appender, runtime);
                        recorded++;
                    }
                } catch (Throwable throwable) {
                    checkpoint.rollback();
                    active = false;
                    writeIdentity(object, out, runtime);
                }
            } else {
                writeIdentity(object, out, runtime);
            }
        }
    }

    private void writeIdentity(Object object, BinaryOutput out, AgentRuntime runtime) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {
            appender.append(RECORDED_IDENTITY_ONLY);
            ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance().write(object, appender, runtime);
        }
    }
}
