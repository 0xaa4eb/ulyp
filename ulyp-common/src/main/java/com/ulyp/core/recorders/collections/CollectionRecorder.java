package com.ulyp.core.recorders.collections;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.bytes.Mark;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.util.LoggingSettings;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

@ThreadSafe
public class CollectionRecorder extends ObjectRecorder {

    protected static final int RECORDED_ELEMENTS_FLAG = 1;
    protected static final int RECORDED_IDENTITY_FLAG = 0;

    private final Logger log = LoggerFactory.getLogger(getClass());
    @Setter
    private int maxElementsToRecord;
    private List<CollectionsRecordingMode> modes = Collections.singletonList(CollectionsRecordingMode.NONE);
    private volatile boolean active = true;

    public CollectionRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return modes.stream().anyMatch(mode -> mode.supports(type)) && Collection.class.isAssignableFrom(type);
    }

    @Override
    public boolean supportsAsyncRecording() {
        return false;
    }

    public void setModes(List<CollectionsRecordingMode> modes) {
        this.modes = modes;
        log.info("Collection recording modes set to {}", modes);
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        return read(objectType, CollectionType.OTHER, input, typeResolver);
    }

    protected ObjectRecord read(@NotNull Type type, CollectionType collectionType, BytesIn input, ByIdTypeResolver typeResolver) {
        int recordedElements = input.readInt();

        if (recordedElements == RECORDED_ELEMENTS_FLAG) {
            int collectionSize = input.readInt();
            List<ObjectRecord> elements = new ArrayList<>();
            int recordedElementsCount = input.readInt();

            for (int i = 0; i < recordedElementsCount; i++) {
                elements.add(input.readObject(typeResolver));
            }
            return new CollectionRecord(
                    type,
                    collectionType,
                    collectionSize,
                    elements
            );
        } else {
            return ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().read(type, input, typeResolver);
        }
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        if (active) {
            Mark mark = out.mark();
            out.write(RECORDED_ELEMENTS_FLAG);
            try {
                Collection<?> collection = (Collection<?>) object;
                int length = collection.size();
                out.write(length);
                int elementsToRecord = Math.min(maxElementsToRecord, length);
                out.write(elementsToRecord);
                Iterator<?> iterator = collection.iterator();
                int recorded = 0;

                while (recorded < elementsToRecord && iterator.hasNext()) {
                    out.write(iterator.next(), typeResolver);
                    recorded++;
                }
            } catch (Throwable throwable) {
                if (LoggingSettings.INFO_ENABLED) {
                    log.info("Collection elements will not be recorded as error occurred while recording for class {}", object.getClass(), throwable);
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
