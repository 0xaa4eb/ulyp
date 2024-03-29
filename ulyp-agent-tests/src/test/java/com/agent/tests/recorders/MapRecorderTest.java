package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.*;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import com.ulyp.core.recorders.collections.MapEntryRecord;
import com.ulyp.core.recorders.collections.MapRecord;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class MapRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordSimpleMap() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnHashMap")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        MapRecord collection = (MapRecord) root.getReturnValue();

        Assert.assertEquals(2, collection.getSize());

        List<MapEntryRecord> entries = collection.getEntries();
        MapEntryRecord firstEntry = entries.get(0);
        StringObjectRecord key = (StringObjectRecord) firstEntry.getKey();
        Assert.assertEquals("a", key.value());
        StringObjectRecord value = (StringObjectRecord) firstEntry.getValue();
        Assert.assertEquals("b", value.value());
    }

    @Test
    public void shouldNotRecordCustomMapIfOnlyJavaMapsAreRecorded() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnHashMap")
                        .withRecordCollections(CollectionsRecordingMode.JAVA)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(IdentityObjectRecord.class));
    }

    @Test
    public void shouldFallbackToIdentityIfRecordingFailed() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnMapThrowingOnIteration")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(IdentityObjectRecord.class));
    }

    @Test
    public void shouldFallbackToIdentityIfRecordingOfMapValueFailed() {

        CallRecord root = runSubprocessAndReadFile(
            new ForkProcessBuilder()
                .withMainClassName(TestCase.class)
                .withMethodToRecord("returnMapWithObjectsThrowingOnPrint")
                .withRecordCollections(CollectionsRecordingMode.ALL)
                .withPrintClasses("**.XYZ")
        );

        MapRecord collection = (MapRecord) root.getReturnValue();

        Assert.assertEquals(2, collection.getSize());

        List<MapEntryRecord> entries = collection.getEntries();
        MapEntryRecord firstEntry = entries.get(0);
        Assert.assertTrue(firstEntry.getValue() instanceof IdentityObjectRecord);
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
