package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.*;
import com.ulyp.storage.CallRecord;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CollectionRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordSimpleItemsProperly() {

        CallRecord root = run(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnArrayListOfString")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        CollectionRecord collection = (CollectionRecord) root.getReturnValue();

        List<ObjectRecord> items = collection.getRecordedItems();

        StringObjectRecord firstItemRepr = (StringObjectRecord) items.get(0);
        Assert.assertEquals("a", firstItemRepr.value());

        StringObjectRecord secondItemRepr = (StringObjectRecord) items.get(1);
        Assert.assertEquals("b", secondItemRepr.value());
    }

    @Test
    public void shouldRecordSimpleListIfAllCollectionsAreRecorded() {

        CallRecord root = run(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnArrayListOfString")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(CollectionRecord.class));
    }

    @Test
    public void shouldRecordSimpleListIfJavaCollectionsAreRecorded() {

        CallRecord root = run(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnArrayListOfString")
                        .withRecordCollections(CollectionsRecordingMode.JAVA)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(CollectionRecord.class));
    }

    @Test
    public void shouldNotRecordAnythingIfSpecifiedToRecordOnlyJavaCollection() {

        CallRecord root = run(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnCustomList")
                        .withRecordCollections(CollectionsRecordingMode.JAVA)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(IdentityObjectRecord.class));
    }

    @Test
    public void shouldRecordCustomListIfSpecifiedAllCollectionsToRecord() {

        CallRecord root = run(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnCustomList")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(CollectionRecord.class));
    }

    @Test
    @Ignore
    public void shouldFallbackToIdentityIfRecordingFailed() {

        CallRecord root = run(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnThrowingOnIteratorList")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(IdentityObjectRecord.class));
    }

    static class TestCase {

        public static List<String> returnArrayListOfString() {
            return Arrays.asList("a", "b");
        }

        public static List<String> returnCustomList() {
            return new ArrayList<String>() {
                {
                    add("a");
                    add("b");
                }
            };
        }

        public static List<String> returnThrowingOnIteratorList() {
            return new ArrayList<String>() {
                {
                    add("a");
                    add("b");
                }

                @NotNull
                @Override
                public Iterator<String> iterator() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public static void main(String[] args) {
            System.out.println(returnArrayListOfString());
            System.out.println(returnCustomList());
            List<String> strings = returnThrowingOnIteratorList();
            System.out.println(System.identityHashCode(strings));
        }
    }
}
