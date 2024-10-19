package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.core.recorders.collections.CollectionRecord;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CollectionRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldNotRecordCollectionByDefault() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnArrayListOfString")
        );

        assertInstanceOf(IdentityObjectRecord.class, root.getReturnValue());
    }

    @Test
    void shouldRecordCollectionItems() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnArrayListOfString")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        CollectionRecord collection = (CollectionRecord) root.getReturnValue();

        assertEquals(6, collection.getLength());

        List<ObjectRecord> items = collection.getRecordedItems();

        assertEquals("a", ((StringObjectRecord) items.get(0)).value());
        assertEquals("b", ((StringObjectRecord) items.get(1)).value());
        assertEquals("c", ((StringObjectRecord) items.get(2)).value());
    }

    @Test
    void shouldRecordCollectionItemsMoreItemsIfPropSet() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnArrayListOfString")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
                        .withRecordCollectionItems(5)
        );

        CollectionRecord collection = (CollectionRecord) root.getReturnValue();

        assertEquals(6, collection.getLength());

        List<ObjectRecord> items = collection.getRecordedItems();

        assertEquals("a", ((StringObjectRecord) items.get(0)).value());
        assertEquals("b", ((StringObjectRecord) items.get(1)).value());
        assertEquals("c", ((StringObjectRecord) items.get(2)).value());
        assertEquals("d", ((StringObjectRecord) items.get(3)).value());
        assertEquals("e", ((StringObjectRecord) items.get(4)).value());
    }

    @Test
    void shouldRecordSimpleListIfAllCollectionsAreRecorded() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnArrayListOfString")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        assertThat(root.getReturnValue(), Matchers.instanceOf(CollectionRecord.class));
    }

    @Test
    void shouldRecordSimpleListIfJavaCollectionsAreRecorded() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnArrayListOfString")
                        .withRecordCollections(CollectionsRecordingMode.JDK)
        );

        assertThat(root.getReturnValue(), Matchers.instanceOf(CollectionRecord.class));
    }

    @Test
    void shouldNotRecordAnythingIfSpecifiedToRecordOnlyJavaCollection() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnCustomList")
                        .withRecordCollections(CollectionsRecordingMode.JDK)
        );

        assertThat(root.getReturnValue(), Matchers.instanceOf(IdentityObjectRecord.class));
    }

    @Test
    void shouldRecordCustomListIfSpecifiedAllCollectionsToRecord() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnCustomList")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        assertThat(root.getReturnValue(), Matchers.instanceOf(CollectionRecord.class));
    }

    @Test
    void shouldFallbackToIdentityIfRecordingFailed() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnThrowingOnIteratorList")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        assertThat(root.getReturnValue(), Matchers.instanceOf(IdentityObjectRecord.class));
    }

    static class TestCase {

        public static List<String> returnArrayListOfString() {
            return Arrays.asList("a", "b", "c", "d", "e", "f");
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
