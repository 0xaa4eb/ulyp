package com.ulyp.core.recorders.collections;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.Checkpoint;
import com.ulyp.core.util.SystemPropertyUtil;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapRecorder extends ObjectRecorder {

    public static final int MAX_ITEMS_TO_RECORD = SystemPropertyUtil.getInt("ulyp.recorder.map.items", 3);
    private static final int RECORDED_ITEMS_FLAG = 1;
    private static final int RECORDED_IDENTITY_ONLY = 0;
    @Setter
    private volatile CollectionsRecordingMode mode;
    private volatile boolean active = true;

    public MapRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return Map.class.isAssignableFrom(type) && mode.supports(type);
    }

    @Override
    public ObjectRecord read(@NotNull Type type, BinaryInput input, ByIdTypeResolver typeResolver) {
        int recordedItems = input.readInt();

        if (recordedItems == RECORDED_ITEMS_FLAG) {
            int collectionSize = input.readInt();
            int recordedItemsCount = input.readInt();
            List<MapEntryRecord> entries = new ArrayList<>();
            for (int i = 0; i < recordedItemsCount; i++) {
                ObjectRecord key = input.readObject(typeResolver);
                ObjectRecord value = input.readObject(typeResolver);
                entries.add(new MapEntryRecord(Type.unknown(), key, value));
            }
            return new MapRecord(type, collectionSize, entries);
        } else {
            return ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().read(type, input, typeResolver);
        }
    }

    @Override
    public void write(Object object, BinaryOutput nout, TypeResolver typeResolver) throws Exception {
        try (BinaryOutput out = nout.nest()) {
            if (active) {
                Checkpoint checkpoint = out.checkpoint();
                out.write(RECORDED_ITEMS_FLAG);
                try {
                    Map<?, ?> collection = (Map<?, ?>) object;
                    int length = collection.size();
                    out.write(length);
                    int itemsToRecord = Math.min(MAX_ITEMS_TO_RECORD, length);
                    out.write(itemsToRecord);
                    Iterator<? extends Map.Entry<?, ?>> iterator = collection.entrySet().iterator();
                    int recorded = 0;

                    while (recorded < itemsToRecord && iterator.hasNext()) {
                        Map.Entry<?, ?> entry = iterator.next();
                        out.write(entry.getKey(), typeResolver);
                        out.write(entry.getValue(), typeResolver);
                        recorded++;
                    }
                } catch (Throwable throwable) {
                    checkpoint.rollback();
                    active = false; // TODO ban by id
                    writeMapIdentity(object, out, typeResolver);
                }
            } else {
                writeMapIdentity(object, out, typeResolver);
            }
        }

    }

    private void writeMapIdentity(Object object, BinaryOutput out, TypeResolver runtime) throws Exception {
        try (BinaryOutput nestedOut = out.nest()) {
            nestedOut.write(RECORDED_IDENTITY_ONLY);
            ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().write(object, nestedOut, runtime);
        }
    }
}
