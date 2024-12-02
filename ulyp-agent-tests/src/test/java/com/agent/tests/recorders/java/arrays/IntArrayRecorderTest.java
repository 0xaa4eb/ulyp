package com.agent.tests.recorders.java.arrays;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingMatchers;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.arrays.ArrayRecord;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class IntArrayRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldNotRecordArrayValuesByDefault() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnArray")
        );


        assertThat(root.getReturnValue().getType().getName(), is("[I"));
    }

    @Test
    void shouldRecordIntArray() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnArray")
                        .withRecordArrays()
        );


        ArrayRecord array = (ArrayRecord) root.getReturnValue();

        assertThat(array.getLength(), is(5));

        List<? extends ObjectRecord> elements = array.getElements();

        assertThat(elements, Matchers.hasSize(3));
        assertThat(elements.get(0), RecordingMatchers.isIntegral(1));
        assertThat(elements.get(1), RecordingMatchers.isIntegral(2));
        assertThat(elements.get(2), RecordingMatchers.isIntegral(3));
    }

    @Test
    void shouldRecordMoreElementsForIntArrayIfConfigured() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnArray")
                        .withRecordArrays()
                        .withRecordArrayItems(5)
        );


        ArrayRecord array = (ArrayRecord) root.getReturnValue();

        assertThat(array.getLength(), is(5));

        List<? extends ObjectRecord> elements = array.getElements();

        assertThat(elements, Matchers.hasSize(5));
        assertThat(elements.get(0), RecordingMatchers.isIntegral(1));
        assertThat(elements.get(1), RecordingMatchers.isIntegral(2));
        assertThat(elements.get(2), RecordingMatchers.isIntegral(3));
        assertThat(elements.get(3), RecordingMatchers.isIntegral(4));
        assertThat(elements.get(4), RecordingMatchers.isIntegral(5));
    }

    public static class TestCase {

        public static int[] returnArray() {
            return new int[]{1, 2, 3, 4, 5};
        }

        public static void main(String[] args) {
            System.out.println(returnArray());
        }
    }
}
