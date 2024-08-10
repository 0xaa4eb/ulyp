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
    void shouldRecordSimpleMap() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnHashMap")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        MapRecord collection = (MapRecord) root.getReturnValue();

        assertEquals(2, collection.getSize());

        List<MapEntryRecord> entries = collection.getEntries();
        MapEntryRecord firstEntry = entries.get(0);
        StringObjectRecord key = (StringObjectRecord) firstEntry.getKey();
        assertEquals("a", key.value());
        StringObjectRecord value = (StringObjectRecord) firstEntry.getValue();
        assertEquals("b", value.value());
    }

    @Test
    void shouldNotRecordCustomMapIfOnlyJavaMapsAreRecorded() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnHashMap")
                        .withRecordCollections(CollectionsRecordingMode.JAVA)
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
                .withPrintClasses("**.XYZ")
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
                    put("a", "b");
                    put("c", "d");
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
