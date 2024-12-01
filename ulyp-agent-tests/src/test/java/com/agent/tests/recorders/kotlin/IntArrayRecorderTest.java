package com.agent.tests.recorders.kotlin;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingMatchers;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.arrays.ArrayRecord;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class IntArrayRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordIntArray() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.CollectionsTestKt.getArray"))
                        .withRecordArrays()
        );


        ArrayRecord record = (ArrayRecord) root.getReturnValue();

        assertThat(record.getLength(), is(5));

        List<? extends ObjectRecord> elements = record.getElements();

        assertThat(elements, Matchers.hasSize(3));
        assertThat(elements.get(0), RecordingMatchers.isIntegral(1));
        assertThat(elements.get(1), RecordingMatchers.isIntegral(2));
        assertThat(elements.get(2), RecordingMatchers.isIntegral(3));
    }

    public static class TestCase {

        public static void main(String[] args) {
            System.out.println(CollectionsTestKt.getArray());
        }
    }
}
