package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import com.ulyp.core.recorders.collections.MapEntryRecord;
import com.ulyp.core.recorders.collections.MapRecord;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class MapRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordMapItems() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnHashMap")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        MapRecord collection = (MapRecord) root.getReturnValue();

        assertEquals(7, collection.getSize());

        List<MapEntryRecord> entries = collection.getEntries();
        assertEquals(entries.size(), 3);

        assertEquals("A", ((StringObjectRecord) entries.get(0).getKey()).value());
        assertEquals("0", ((StringObjectRecord) entries.get(0).getValue()).value());
        assertEquals("B", ((StringObjectRecord) entries.get(1).getKey()).value());
        assertEquals("1", ((StringObjectRecord) entries.get(1).getValue()).value());
        assertEquals("C", ((StringObjectRecord) entries.get(2).getKey()).value());
        assertEquals("2", ((StringObjectRecord) entries.get(2).getValue()).value());
    }

    @Test
    void shouldRecordMoreMapItemsIfPropSet() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnHashMap")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
                        .withRecordCollectionItems(5)
        );

        MapRecord collection = (MapRecord) root.getReturnValue();

        assertEquals(7, collection.getSize());

        List<MapEntryRecord> entries = collection.getEntries();
        assertEquals(entries.size(), 5);

        assertEquals("A", ((StringObjectRecord) entries.get(0).getKey()).value());
        assertEquals("0", ((StringObjectRecord) entries.get(0).getValue()).value());
        assertEquals("B", ((StringObjectRecord) entries.get(1).getKey()).value());
        assertEquals("1", ((StringObjectRecord) entries.get(1).getValue()).value());
        assertEquals("C", ((StringObjectRecord) entries.get(2).getKey()).value());
        assertEquals("2", ((StringObjectRecord) entries.get(2).getValue()).value());
        assertEquals("D", ((StringObjectRecord) entries.get(3).getKey()).value());
        assertEquals("3", ((StringObjectRecord) entries.get(3).getValue()).value());
        assertEquals("E", ((StringObjectRecord) entries.get(4).getKey()).value());
        assertEquals("4", ((StringObjectRecord) entries.get(4).getValue()).value());
    }

    @Test
    void shouldNotRecordCustomMapIfOnlyJavaMapsAreRecorded() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnHashMap")
                        .withRecordCollections(CollectionsRecordingMode.JDK)
        );

        assertThat(root.getReturnValue(), Matchers.instanceOf(IdentityObjectRecord.class));
    }

    @Test
    void shouldFallbackToIdentityIfRecordingFailed() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnMapThrowingOnIteration")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        assertThat(root.getReturnValue(), Matchers.instanceOf(IdentityObjectRecord.class));
    }

    @Test
    void shouldFallbackToIdentityIfRecordingOfMapValueFailed() {

        CallRecord root = runSubprocessAndReadFile(
            new ForkProcessBuilder()
                .withMainClassName(TestCase.class)
                .withMethodToRecord("returnMapWithObjectsThrowingOnPrint")
                .withRecordCollections(CollectionsRecordingMode.ALL)
                .withPrintTypes("**.XYZ")
        );

        MapRecord collection = (MapRecord) root.getReturnValue();

        assertEquals(2, collection.getSize());

        List<MapEntryRecord> entries = collection.getEntries();
        MapEntryRecord firstEntry = entries.get(0);
        assertInstanceOf(IdentityObjectRecord.class, firstEntry.getValue());
    }

    static class XYZ {
        @Override
        public String toString() {
            throw new RuntimeException("not supported");
        }
    }

    static class TestCase {

        public static Map<String, XYZ> returnMapWithObjectsThrowingOnPrint() {
            return new LinkedHashMap<String, XYZ>() {
                {
                    put("a", new XYZ());
                    put("c", new XYZ());
                }
            };
        }

        public static Map<String, String> returnHashMap() {
            return new LinkedHashMap<String, String>() {
                {
                    put("A", "0");
                    put("B", "1");
                    put("C", "2");
                    put("D", "3");
                    put("E", "4");
                    put("F", "5");
                    put("G", "6");
                }
            };
        }

        public static Map<String, String> returnMapThrowingOnIteration() {
            return new HashMap<String, String>() {
                {
                    put("a", "b");
                    put("c", "d");
                }

                @Override
                public Set<Entry<String, String>> entrySet() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public static void main(String[] args) {
            System.out.println(returnHashMap());
            Map<String, String> stringStringMap = returnMapThrowingOnIteration();
            System.out.println(System.identityHashCode(stringStringMap));
            try {
                System.out.println(System.identityHashCode(returnMapWithObjectsThrowingOnPrint()));
            } catch (Throwable tw) {
                System.out.println(returnMapWithObjectsThrowingOnPrint().size());
            }
        }
    }
}
