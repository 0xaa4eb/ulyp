package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.BinaryOutputAppender;
import com.ulyp.core.recorders.bytes.Checkpoint;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class CollectionRecorder extends ObjectRecorder {

    public static final int MAX_ITEMS_TO_RECORD = 3;
    private static final int RECORDED_ITEMS_FLAG = 1;
    private static final int RECORDED_IDENTITY_FLAG = 0;

    private volatile boolean active = true;
    private CollectionsRecordingMode mode = CollectionsRecordingMode.NONE;

    protected CollectionRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return mode.supports(type) && type.isCollection();
    }

    public void setMode(CollectionsRecordingMode mode) {
        this.mode = mode;
        log.info("Set collection recording mode to {}", mode);
    }

    @Override
    public ObjectRecord read(Type classDescription, BinaryInput input, ByIdTypeResolver typeResolver) {
        int recordedItems = input.readInt();

        if (recordedItems == RECORDED_ITEMS_FLAG) {
            int collectionSize = input.readInt();
            List<ObjectRecord> items = new ArrayList<>();
            int recordedItemsCount = input.readInt();

            for (int i = 0; i < recordedItemsCount; i++) {
                items.add(input.readObject(typeResolver));
            }
            return new CollectionRecord(
                    classDescription,
                    collectionSize,
                    items
            );
        } else {
            return ObjectRecorderType.IDENTITY_RECORDER.getInstance().read(classDescription, input, typeResolver);
        }
    }

    @Override
    public void write(Object object, Type type, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {

            // TODO per type statistics
            if (active) {
                appender.append(RECORDED_ITEMS_FLAG);
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
                        appender.append(iterator.next(), typeResolver);
                        recorded++;
                    }
                } catch (Throwable throwable) {
                    log.info("Collection items will not be recorded as error occurred while recording", throwable);
                    checkpoint.rollback();
                    active = false;
                    writeIdentity(object, out, typeResolver);
                }
            } else {
                writeIdentity(object, out, typeResolver);
            }
        }
    }

    private void writeIdentity(Object object, BinaryOutput out, TypeResolver runtime) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {
            appender.append(RECORDED_IDENTITY_FLAG);
            ObjectRecorderType.IDENTITY_RECORDER.getInstance().write(object, appender, runtime);
        }
    }
}
