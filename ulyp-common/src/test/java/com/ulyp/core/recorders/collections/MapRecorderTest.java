package com.ulyp.core.recorders.collections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.ToStringPrintingRecorder;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.BufferBinaryInput;
import com.ulyp.core.recorders.bytes.BufferBinaryOutput;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.core.util.TypeMatcher;

import static org.junit.Assert.*;

public class MapRecorderTest {

    static class XYZ {
        @Override
        public String toString() {
            throw new RuntimeException("not supported");
        }
    }

    private final UnsafeBuffer buffer = new UnsafeBuffer(new byte[16 * 1024]);
    private final BinaryOutput out = new BufferBinaryOutput(buffer);
    private final BinaryInput in = new BufferBinaryInput(buffer);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final MapRecorder mapRecorder = new MapRecorder((byte) 1);
    private final ToStringPrintingRecorder printingRecorder = (ToStringPrintingRecorder) ObjectRecorderRegistry.TO_STRING_RECORDER.getInstance();

    @Test
    public void test() throws Exception {
        printingRecorder.addClassesToPrint(new HashSet<>(Arrays.asList(TypeMatcher.parse("**.XYZ"))));
        Map<String, XYZ> map = new HashMap<>();
        map.put("ABC", new XYZ());
        map.put("ZXC", new XYZ());

        mapRecorder.write(map, out, typeResolver);

        MapRecord mapRecord = (MapRecord) mapRecorder.read(typeResolver.get(map), in, typeResolver::get);

        assertEquals(2, mapRecord.getEntries().size());

        List<MapEntryRecord> entries = mapRecord.getEntries();
        MapEntryRecord mapEntryRecord = entries.get(0);
        assertTrue(mapEntryRecord.getValue() instanceof IdentityObjectRecord);
    }

    @Test
    public void test2() throws Exception {
        Map<String, XYZ> map = new HashMap<String, XYZ>() {
            @Override
            public Set<Entry<String, XYZ>> entrySet() {
                throw new RuntimeException("unsupported");
            }
        };
        map.put("ABC", new XYZ());
        map.put("ZXC", new XYZ());

        mapRecorder.setMode(CollectionsRecordingMode.ALL);
        mapRecorder.write(map, out, typeResolver);

        IdentityObjectRecord mapRecord = (IdentityObjectRecord) mapRecorder.read(typeResolver.get(map), in, typeResolver::get);

        System.out.println(mapRecord);
    }
}