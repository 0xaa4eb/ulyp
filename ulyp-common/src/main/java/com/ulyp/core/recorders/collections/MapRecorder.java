package com.ulyp.core.recorders.collections;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import lombok.Setter;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.bytes.Mark;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@ThreadSafe
public class MapRecorder extends ObjectRecorder {

    private static final byte RECORDED_ENTRIES_FLAG = 1;
    private static final byte RECORDED_IDENTITY_ONLY = 0;

    @Setter
    private int maxEntriesToRecord;
    @Setter
    private volatile CollectionsRecordingMode mode = CollectionsRecordingMode.NONE;
    private volatile boolean active = true;

    public MapRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return mode.supports(type) && Map.class.isAssignableFrom(type);
    }

    @Override
    public boolean supportsAsyncRecording() {
        return false;
    }

    @Override
    public ObjectRecord read(@NotNull Type type, BytesIn input, ByIdTypeResolver typeResolver) {
        byte recordedEntries = input.readByte();

        if (recordedEntries == RECORDED_ENTRIES_FLAG) {
            int collectionSize = input.readVarInt();
            int recordedEntriesCount = input.readVarInt();
            List<MapEntryRecord> entries = new ArrayList<>();
            for (int i = 0; i < recordedEntriesCount; i++) {
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
    public void write(Object object, BytesOut nout, TypeResolver typeResolver) throws Exception {
        try (BytesOut out = nout.nest()) {
            if (active) {
                Mark mark = out.mark();
                out.write(RECORDED_ENTRIES_FLAG);
                try {
                    Map<?, ?> collection = (Map<?, ?>) object;
                    int length = collection.size();
                    out.writeVarInt(length);
                    int entriesToRecord = Math.min(maxEntriesToRecord, length);
                    out.writeVarInt(entriesToRecord);
                    Iterator<? extends Map.Entry<?, ?>> iterator = collection.entrySet().iterator();
                    int recorded = 0;

                    while (recorded < entriesToRecord && iterator.hasNext()) {
                        Map.Entry<?, ?> entry = iterator.next();
                        out.write(entry.getKey(), typeResolver);
                        out.write(entry.getValue(), typeResolver);
                        recorded++;
                    }
                } catch (Throwable throwable) {
                    mark.rollback();
                    active = false; // TODO ban by id
                    writeMapIdentity(object, out, typeResolver);
                } finally {
                    mark.close();
                }
            } else {
                writeMapIdentity(object, out, typeResolver);
            }
        }
    }

    private void writeMapIdentity(Object object, BytesOut out, TypeResolver runtime) throws Exception {
        try (BytesOut nestedOut = out.nest()) {
            nestedOut.write(RECORDED_IDENTITY_ONLY);
            ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().write(object, nestedOut, runtime);
        }
    }
}
