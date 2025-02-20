package com.ulyp.core.recorders.arrays;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.numeric.IntegralRecord;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Int array recorder
 */
@ThreadSafe
public class IntArrayRecorder extends ObjectRecorder {

    // Intentionally not volatile
    @Setter
    private boolean enabled = false;
    @Setter
    private int maxItemsToRecord;

    public IntArrayRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return enabled && type == int[].class;
    }

    @Override
    public ArrayRecord read(@NotNull Type type, BytesIn input, ByIdTypeResolver typeResolver) {
        int arrayLength = input.readVarInt();
        int recordedItemsCount = input.readVarInt();
        List<IntegralRecord> elements = IntStream.range(0, recordedItemsCount)
                .mapToObj(ignored -> new IntegralRecord(Type.INT, input.readVarInt()))
                .collect(Collectors.toList());
        return new ArrayRecord(type, arrayLength, elements);
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        int[] array = (int[]) object;
        int length = array.length;
        out.writeVarInt(length);
        int itemsToRecord = Math.min(maxItemsToRecord, length);
        out.writeVarInt(itemsToRecord);

        for (int i = 0; i < itemsToRecord; i++) {
            out.writeVarInt(array[i]);
        }
    }
}
