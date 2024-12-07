package com.ulyp.core.recorders.collections;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BufferBytesOut;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.bytes.DirectBytesIn;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.PrintingRecorder;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.core.util.TypeMatcher;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapRecorderTest {

    static class XYZ {
        @Override
        public String toString() {
            throw new RuntimeException("not supported");
        }
    }

    private final UnsafeBuffer buffer = new UnsafeBuffer(new byte[16 * 1024]);
    private final BytesOut out = new BufferBytesOut(buffer);
    private final BytesIn in = new DirectBytesIn(buffer);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final MapRecorder mapRecorder = new MapRecorder((byte) 1);
    private final PrintingRecorder printingRecorder = (PrintingRecorder) ObjectRecorderRegistry.TO_STRING_RECORDER.getInstance();

    @BeforeEach
    public void setUp() {
        mapRecorder.setMaxEntriesToRecord(3);
    }

    @Test
    void test() throws Exception {
        printingRecorder.addTypeMatchers(Arrays.asList(TypeMatcher.parse("**.XYZ")));
        Map<String, XYZ> map = new HashMap<>();
        map.put("ABC", new XYZ());
        map.put("ZXC", new XYZ());

        mapRecorder.setModes(Collections.singletonList(CollectionsRecordingMode.ALL));

        mapRecorder.write(map, out, typeResolver);

        MapRecord mapRecord = (MapRecord) mapRecorder.read(typeResolver.get(map), in, typeResolver::get);

        assertEquals(2, mapRecord.getEntries().size());

        List<MapEntryRecord> entries = mapRecord.getEntries();
        MapEntryRecord mapEntryRecord = entries.get(0);
        Assertions.assertInstanceOf(IdentityObjectRecord.class, mapEntryRecord.getValue());
    }

    @Test
    void test2() throws Exception {
        Map<String, XYZ> map = new HashMap<String, XYZ>() {
            @Override
            public Set<Entry<String, XYZ>> entrySet() {
                throw new RuntimeException("unsupported");
            }
        };
        map.put("ABC", new XYZ());
        map.put("ZXC", new XYZ());

        mapRecorder.setModes(Collections.singletonList(CollectionsRecordingMode.ALL));
        mapRecorder.write(map, out, typeResolver);

        IdentityObjectRecord mapRecord = (IdentityObjectRecord) mapRecorder.read(typeResolver.get(map), in, typeResolver::get);

        System.out.println(mapRecord);
    }
}