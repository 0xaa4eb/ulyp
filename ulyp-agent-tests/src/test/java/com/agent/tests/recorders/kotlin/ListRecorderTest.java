package com.agent.tests.recorders.kotlin;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingMatchers;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.collections.CollectionRecord;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ListRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordEmptyList() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.CollectionsTestKt.getEmptyList"))
                        .withRecordCollections(CollectionsRecordingMode.JDK, CollectionsRecordingMode.KT)
        );

        CollectionRecord collection = (CollectionRecord) root.getReturnValue();

        assertEquals(0, collection.getSize());
    }

    @Test
    void shouldRecordReversedList() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.CollectionsTestKt.getReversedList"))
                        .withRecordCollections(CollectionsRecordingMode.JDK, CollectionsRecordingMode.KT)
                        .withRecordConstructors()
        );

        CollectionRecord collection = (CollectionRecord) root.getReturnValue();

        List<ObjectRecord> elements = collection.getElements();
        assertThat(elements.get(0), RecordingMatchers.isString("F"));
        assertThat(elements.get(1), RecordingMatchers.isString("E"));
        assertThat(elements.get(2), RecordingMatchers.isString("D"));
    }

    @Test
    void shouldRecordImmutableListEntries() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.CollectionsTestKt.getImmutableList"))
                        .withRecordCollections(CollectionsRecordingMode.JDK)
        );

        CollectionRecord collection = (CollectionRecord) root.getReturnValue();

        assertEquals("java.util.Arrays$ArrayList", collection.getType().getName());
        assertEquals(5, collection.getSize());

        List<ObjectRecord> elements = collection.getElements();
        assertEquals(3, elements.size());

        assertThat(elements.get(0), RecordingMatchers.isString("ABC"));
        assertThat(elements.get(1), RecordingMatchers.isString("CDE"));
        assertThat(elements.get(2), RecordingMatchers.isString("EFG"));
    }

    @Test
    void shouldRecordArrayDequeue() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.CollectionsTestKt.getArrayDequeue"))
                        .withRecordCollections(CollectionsRecordingMode.JDK, CollectionsRecordingMode.KT)
        );

        CollectionRecord collection = (CollectionRecord) root.getReturnValue();

        assertEquals(5, collection.getSize());
        List<ObjectRecord> elements = collection.getElements();
        assertEquals(3, elements.size());

        assertThat(elements.get(0), RecordingMatchers.isString("A"));
        assertThat(elements.get(1), RecordingMatchers.isString("B"));
        assertThat(elements.get(2), RecordingMatchers.isString("C"));
    }

    @Test
    void shouldRecordMutableListEntries() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.CollectionsTestKt.getMutableList"))
                        .withRecordCollections(CollectionsRecordingMode.JDK)
        );

        CollectionRecord collection = (CollectionRecord) root.getReturnValue();

        assertEquals(ArrayList.class.getName(), collection.getType().getName());
        assertEquals(5, collection.getSize());

        List<ObjectRecord> elements = collection.getElements();
        assertEquals(3, elements.size());

        assertThat(elements.get(0), RecordingMatchers.isString("ABC"));
        assertThat(elements.get(1), RecordingMatchers.isString("CDE"));
        assertThat(elements.get(2), RecordingMatchers.isString("EFG"));
    }

    @Test
    void shouldRecordIdentityForMutableListIfRecordingCollectionIsDisabled() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.CollectionsTestKt.getMutableList"))
                        .withRecordCollections(CollectionsRecordingMode.NONE)
        );

        assertThat(root.getReturnValue(), RecordingMatchers.isIdentity(ArrayList.class.getName()));
    }

    static class TestCase {

        public static void main(String[] args) {
            System.out.println(CollectionsTestKt.getImmutableList());
            System.out.println(CollectionsTestKt.getEmptyList());
            System.out.println(CollectionsTestKt.getMutableList());
            System.out.println(CollectionsTestKt.getArrayDequeue());
            System.out.println(CollectionsTestKt.getReversedList());
        }
    }
}
