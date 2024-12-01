package com.agent.tests.recorders.java;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.collections.CollectionRecord;
import com.ulyp.core.recorders.collections.CollectionType;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import com.ulyp.storage.tree.CallRecord;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.agent.tests.util.RecordingMatchers.isString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CollectionRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldNotRecordCollectionByDefault() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnList")
        );

        assertInstanceOf(IdentityObjectRecord.class, root.getReturnValue());
    }

    @Test
    void shouldRecordElementsForList() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnList")
                        .withRecordCollections(CollectionsRecordingMode.JDK)
        );

        CollectionRecord record = (CollectionRecord) root.getReturnValue();

        assertEquals(CollectionType.LIST, record.getCollectionType());
        assertEquals(6, record.getSize());

        assertThat(record.getElements(), contains(
                isString("A"),
                isString("B"),
                isString("C"))
        );
    }

    @Test
    void shouldRecordElementsForSet() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnSet")
                        .withRecordCollections(CollectionsRecordingMode.JDK)
        );

        CollectionRecord record = (CollectionRecord) root.getReturnValue();

        assertEquals(CollectionType.SET, record.getCollectionType());
        assertEquals(6, record.getSize());

        List<ObjectRecord> items = record.getElements();

        assertThat(items, containsInAnyOrder(
                isString("A"),
                isString("B"),
                isString("C"))
        );
    }

    @Test
    void shouldRecordElementsForQueue() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnQueue")
                        .withRecordCollections(CollectionsRecordingMode.JDK)
        );

        CollectionRecord record = (CollectionRecord) root.getReturnValue();

        assertEquals(CollectionType.QUEUE, record.getCollectionType());
        assertEquals(6, record.getSize());

        List<ObjectRecord> items = record.getElements();

        assertThat(items, containsInAnyOrder(
                isString("Q1"),
                isString("Q2"),
                isString("Q3"))
        );
    }

    @Test
    void shouldRecordElementsForCustomCollection() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnBag")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        CollectionRecord record = (CollectionRecord) root.getReturnValue();

        assertEquals(CollectionType.OTHER, record.getCollectionType());
        assertEquals(6, record.getSize());

        assertThat(record.getElements(), contains(
                isString("A"),
                isString("B"),
                isString("C"))
        );
    }

    @Test
    void shouldRecordCollectionElementsMoreItemsIfPropSet() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnList")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
                        .withRecordCollectionItems(5)
        );

        CollectionRecord collection = (CollectionRecord) root.getReturnValue();

        assertEquals(6, collection.getSize());

        assertThat(collection.getElements(), contains(
                isString("A"),
                isString("B"),
                isString("C"),
                isString("D"),
                isString("E"))
        );
    }

    @Test
    void shouldRecordSimpleListIfAllCollectionsAreRecorded() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnList")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        assertThat(root.getReturnValue(), instanceOf(CollectionRecord.class));
    }

    @Test
    void shouldRecordSimpleListIfJavaCollectionsAreRecorded() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnList")
                        .withRecordCollections(CollectionsRecordingMode.JDK)
        );

        assertThat(root.getReturnValue(), instanceOf(CollectionRecord.class));
    }

    @Test
    void shouldNotRecordAnythingIfSpecifiedToRecordOnlyJavaCollection() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnCustomList")
                        .withRecordCollections(CollectionsRecordingMode.JDK)
        );

        assertThat(root.getReturnValue(), instanceOf(IdentityObjectRecord.class));
    }

    @Test
    void shouldRecordCustomListIfSpecifiedAllCollectionsToRecord() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnCustomList")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        assertThat(root.getReturnValue(), instanceOf(CollectionRecord.class));
    }

    @Test
    void shouldFallbackToIdentityIfRecordingFailed() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnThrowingOnIteratorList")
                        .withRecordCollections(CollectionsRecordingMode.ALL)
        );

        assertThat(root.getReturnValue(), instanceOf(IdentityObjectRecord.class));
    }

    static class TestCase {

        public static List<String> returnList() {
            return Arrays.asList("A", "B", "C", "D", "E", "F");
        }

        public static Set<String> returnSet() {
            return new HashSet<>(Arrays.asList("A", "B", "C", "D", "E", "F"));
        }

        public static Bag<String> returnBag() {
            return new Bag<>(Arrays.asList("A", "B", "C", "D", "E", "F"));
        }

        public static Queue<String> returnQueue() {
            return new ArrayDeque<>(Arrays.asList("Q1", "Q2", "Q3", "Q4", "Q5", "Q6"));
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
            System.out.println(returnList());
            System.out.println(returnSet());
            System.out.println(returnBag());
            System.out.println(returnQueue());
            System.out.println(returnCustomList());
            List<String> strings = returnThrowingOnIteratorList();
            System.out.println(System.identityHashCode(strings));
        }
    }

    static class Bag<E> implements Collection<E> {

        private final List<E> internal;

        public Bag(List<E> from) {
            this.internal = new ArrayList<>(from);
        }

        @Override
        public int size() {
            return internal.size();
        }

        @Override
        public boolean isEmpty() {
            return internal.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return internal.contains(o);
        }

        @Override
        public @NotNull Iterator<E> iterator() {
            return internal.iterator();
        }

        @Override
        public @NotNull Object[] toArray() {
            return internal.toArray();
        }

        @Override
        public @NotNull <T> T[] toArray(@NotNull T[] a) {
            return internal.toArray(a);
        }

        @Override
        public boolean add(E e) {
            return internal.add(e);
        }

        @Override
        public boolean remove(Object o) {
            return internal.remove(o);
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return internal.containsAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends E> c) {
            return internal.addAll(c);
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return internal.removeAll(c);
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return internal.retainAll(c);
        }

        @Override
        public void clear() {
            internal.clear();
        }
    }
}
