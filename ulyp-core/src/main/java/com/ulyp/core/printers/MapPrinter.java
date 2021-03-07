package com.ulyp.core.printers;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.DecodingContext;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;
import com.ulyp.core.printers.bytes.BinaryOutputAppender;
import com.ulyp.core.printers.bytes.Checkpoint;

import java.util.*;

public class MapPrinter extends ObjectBinaryPrinter {

    private CollectionsRecordingMode mode;
    private volatile boolean active = true;

    public static final int MAX_ITEMS_TO_RECORD = 3;

    private static final int RECORDED_ITEMS = 1;
    private static final int RECORDED_IDENTITY_ONLY = 0;

    protected MapPrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(TypeInfo type) {
        return type.isMap() && mode.supports(type);
    }

    public void setMode(CollectionsRecordingMode collectionsRecordingMode) {
        this.mode = collectionsRecordingMode;
    }

    @Override
    public ObjectRepresentation read(TypeInfo classDescription, BinaryInput input, DecodingContext decodingContext) {
        int recordedItems = input.readInt();

        if (recordedItems == RECORDED_ITEMS) {
            int collectionSize = input.readInt();
            int recordedItemsCount = input.readInt();
            List<MapEntryRepresentation> entries = new ArrayList<>();
            for (int i = 0; i < recordedItemsCount; i++) {

                TypeInfo keyType = decodingContext.getType(input.readInt());
                ObjectBinaryPrinter keyPrinter = ObjectBinaryPrinterType.printerForId(input.readByte());
                ObjectRepresentation key = keyPrinter.read(keyType, input, decodingContext);

                TypeInfo valueType = decodingContext.getType(input.readInt());
                ObjectBinaryPrinter valuePrinter = ObjectBinaryPrinterType.printerForId(input.readByte());
                ObjectRepresentation value = valuePrinter.read(valueType, input, decodingContext);

                entries.add(new MapEntryRepresentation(UnknownTypeInfo.getInstance(), key, value));
            }
            return new MapRepresentation(
                    classDescription,
                    collectionSize,
                    entries
            );
        } else {
            return ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance().read(classDescription, input, decodingContext);
        }
    }

    @Override
    public void write(Object object, TypeInfo classDescription, BinaryOutput out, TypeResolver runtime) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {

            if (active) {
                appender.append(RECORDED_ITEMS);
                Checkpoint checkpoint = appender.checkpoint();
                try {
                    Map<?, ?> collection = (Map<?, ?>) object;
                    int length = collection.size();
                    appender.append(length);
                    int itemsToRecord = Math.min(MAX_ITEMS_TO_RECORD, length);
                    appender.append(itemsToRecord);
                    Iterator<? extends Map.Entry<?, ?>> iterator = collection.entrySet().iterator();
                    int recorded = 0;

                    while (recorded < itemsToRecord && iterator.hasNext()) {
                        Map.Entry<?, ?> entry = iterator.next();
                        writeObject(runtime, appender, entry.getKey());
                        writeObject(runtime, appender, entry.getValue());
                        recorded++;
                    }
                } catch (Throwable throwable) {
                    checkpoint.rollback();
                    active = false;
                    writeMapIdentity(object, out, runtime);
                }
            } else {
                writeMapIdentity(object, out, runtime);
            }
        }
    }

    private void writeObject(TypeResolver runtime, BinaryOutputAppender appender, Object item) throws Exception {
        TypeInfo itemType = runtime.get(item);
        appender.append(itemType.getId());
        ObjectBinaryPrinter printer = item != null ? itemType.getSuggestedPrinter() : ObjectBinaryPrinterType.NULL_PRINTER.getInstance();
        appender.append(printer.getId());
        printer.write(item, itemType, appender, runtime);
    }

    private void writeMapIdentity(Object object, BinaryOutput out, TypeResolver runtime) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {
            appender.append(RECORDED_IDENTITY_ONLY);
            ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance().write(object, appender, runtime);
        }
    }
}
