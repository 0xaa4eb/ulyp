package com.test.recorders;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.recorders.*;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class MapRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordSimpleMap() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("returnHashMap")
                        .recordCollections(CollectionsRecordingMode.ALL)
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

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("returnHashMap")
                        .recordCollections(CollectionsRecordingMode.JAVA)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(IdentityObjectRecord.class));
    }

    @Test
    public void shouldFallbackToIdentityIfRecordingFailed() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("returnMapThrowingOnIteration")
                        .recordCollections(CollectionsRecordingMode.ALL)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(IdentityObjectRecord.class));
    }

    static class TestCase {

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
        }
    }
}
