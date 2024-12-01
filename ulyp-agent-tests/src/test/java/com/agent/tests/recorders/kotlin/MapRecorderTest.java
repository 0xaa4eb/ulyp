package com.agent.tests.recorders.kotlin;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingMatchers;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import com.ulyp.core.recorders.collections.MapEntryRecord;
import com.ulyp.core.recorders.collections.MapRecord;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.agent.tests.util.RecordingMatchers.isString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MapRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordImmutableMapEntries() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.CollectionsTestKt.getImmutableMap"))
                        .withRecordCollections(CollectionsRecordingMode.JDK)
        );

        MapRecord collection = (MapRecord) root.getReturnValue();

        assertEquals(LinkedHashMap.class.getName(), collection.getType().getName());
        assertEquals(3, collection.getSize());

        List<MapEntryRecord> entries = collection.getEntries();
        assertEquals(entries.size(), 3);

        // it's a linked hash map, so we can verify in order
        assertThat(entries.get(0).getKey(), isString("1"));
        assertThat(entries.get(0).getValue(), isString("ABC"));
        assertThat(entries.get(1).getKey(), isString("2"));
        assertThat(entries.get(1).getValue(), isString("CDE"));
        assertThat(entries.get(2).getKey(), isString("3"));
        assertThat(entries.get(2).getValue(), isString("EFG"));
    }

    @Test
    void shouldRecordMutableMapEntries() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.CollectionsTestKt.getImmutableMap"))
                        .withRecordCollections(CollectionsRecordingMode.JDK)
        );

        MapRecord collection = (MapRecord) root.getReturnValue();

        assertEquals(LinkedHashMap.class.getName(), collection.getType().getName());
        assertEquals(3, collection.getSize());

        List<MapEntryRecord> entries = collection.getEntries();
        assertEquals(entries.size(), 3);

        // it's a linked hash map, so we can verify in order
        assertThat(entries.get(0).getKey(), isString("1"));
        assertThat(entries.get(0).getValue(), isString("ABC"));
        assertThat(entries.get(1).getKey(), isString("2"));
        assertThat(entries.get(1).getValue(), isString("CDE"));
        assertThat(entries.get(2).getKey(), isString("3"));
        assertThat(entries.get(2).getValue(), isString("EFG"));
    }

    @Test
    void shouldRecordIdentityIfRecordingCollectionIsDisabled() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.CollectionsTestKt.getImmutableMap"))
                        .withRecordCollections(CollectionsRecordingMode.NONE)
        );

        assertThat(root.getReturnValue(), RecordingMatchers.isIdentity(LinkedHashMap.class.getName()));
    }

    static class TestCase {

        public static void main(String[] args) {
            System.out.println(CollectionsTestKt.getImmutableMap());
            System.out.println(CollectionsTestKt.getMutableMap());
        }
    }
}
