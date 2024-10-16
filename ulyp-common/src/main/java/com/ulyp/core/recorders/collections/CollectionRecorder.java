package com.ulyp.core.recorders.collections;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.bytes.Mark;
import com.ulyp.core.util.LoggingSettings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class CollectionRecorder extends ObjectRecorder {

    private static final int RECORDED_ITEMS_FLAG = 1;
    private static final int RECORDED_IDENTITY_FLAG = 0;

    private volatile boolean active = true;
    @Setter
    private int maxItemsToRecord;
    private CollectionsRecordingMode mode = CollectionsRecordingMode.NONE;

    public CollectionRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return mode.supports(type) && Collection.class.isAssignableFrom(type);
    }

    @Override
    public boolean supportsAsyncRecording() {
        return false;
    }

    public void setMode(CollectionsRecordingMode mode) {
        this.mode = mode;
        log.info("Set collection recording mode to {}", mode);
    }

    @Override
    public ObjectRecord read(@NotNull Type type, BytesIn input, ByIdTypeResolver typeResolver) {
        int recordedItems = input.readInt();

        if (recordedItems == RECORDED_ITEMS_FLAG) {
            int collectionSize = input.readInt();
            List<ObjectRecord> items = new ArrayList<>();
            int recordedItemsCount = input.readInt();

            for (int i = 0; i < recordedItemsCount; i++) {
                items.add(input.readObject(typeResolver));
            }
            return new CollectionRecord(
                    type,
                    collectionSize,
                    items
            );
        } else {
            return ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().read(type, input, typeResolver);
        }
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        if (active) {
            Mark mark = out.mark();
            out.write(RECORDED_ITEMS_FLAG);
            try {
                Collection<?> collection = (Collection<?>) object;
                int length = collection.size();
                out.write(length);
                int itemsToRecord = Math.min(maxItemsToRecord, length);
                out.write(itemsToRecord);
                Iterator<?> iterator = collection.iterator();
                int recorded = 0;

                while (recorded < itemsToRecord && iterator.hasNext()) {
                    out.write(iterator.next(), typeResolver);
                    recorded++;
                }
            } catch (Throwable throwable) {
                if (LoggingSettings.INFO_ENABLED) {
                    log.info("Collection items will not be recorded as error occurred while recording", throwable);
                }
                mark.rollback();
                active = false; // TODO ban by id
                writeIdentity(object, out, typeResolver);
            } finally {
                mark.close();
            }
        } else {
            writeIdentity(object, out, typeResolver);
        }
    }

    private void writeIdentity(Object object, BytesOut out, TypeResolver runtime) throws Exception {
        try (BytesOut nestedOut = out.nest()) {
            nestedOut.write(RECORDED_IDENTITY_FLAG);
            ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().write(object, nestedOut, runtime);
        }
    }
}
