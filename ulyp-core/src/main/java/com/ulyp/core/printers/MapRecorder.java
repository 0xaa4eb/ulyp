package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.TypeTrait;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;
import com.ulyp.core.printers.bytes.BinaryOutputAppender;
import com.ulyp.core.printers.bytes.Checkpoint;

import java.util.*;

public class MapRecorder extends ObjectRecorder {

    private CollectionsRecordingMode mode;
    private volatile boolean active = true;

    public static final int MAX_ITEMS_TO_RECORD = 3;

    private static final int RECORDED_ITEMS = 1;
    private static final int RECORDED_IDENTITY_ONLY = 0;

    protected MapRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.getTraits().contains(TypeTrait.MAP) && mode.supports(type);
    }

    public void setMode(CollectionsRecordingMode collectionsRecordingMode) {
        this.mode = collectionsRecordingMode;
    }

    @Override
    public ObjectRepresentation read(Type classDescription, BinaryInput input, ByIdTypeResolver typeResolver) {
        int recordedItems = input.readInt();

        if (recordedItems == RECORDED_ITEMS) {
            int collectionSize = input.readInt();
            int recordedItemsCount = input.readInt();
            List<MapEntryRepresentation> entries = new ArrayList<>();
            for (int i = 0; i < recordedItemsCount; i++) {
                ObjectRepresentation key = input.readObject(typeResolver);
                ObjectRepresentation value = input.readObject(typeResolver);
                entries.add(new MapEntryRepresentation(Type.unknown(), key, value));
            }
            return new MapRepresentation(
                    classDescription,
                    collectionSize,
                    entries
            );
        } else {
            return ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance().read(classDescription, input, typeResolver);
        }
    }

    @Override
    public void write(Object object, Type classDescription, BinaryOutput out, TypeResolver typeResolver) throws Exception {
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
                        appender.append(entry.getKey(), typeResolver);
                        appender.append(entry.getValue(), typeResolver);
                        recorded++;
                    }
                } catch (Throwable throwable) {
                    checkpoint.rollback();
                    active = false;
                    writeMapIdentity(object, out, typeResolver);
                }
            } else {
                writeMapIdentity(object, out, typeResolver);
            }
        }
    }

    private void writeMapIdentity(Object object, BinaryOutput out, TypeResolver runtime) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {
            appender.append(RECORDED_IDENTITY_ONLY);
            ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance().write(object, appender, runtime);
        }
    }
}
