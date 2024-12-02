package com.agent.tests.recorders.java;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.arrays.ArrayRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static com.agent.tests.util.RecordingMatchers.isIdentity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RecursiveArrayRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldStopRecordingInfiniteRecursionAtSomeLevel() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnArray")
                        .withRecordArrays()
        );

        ArrayRecord array = (ArrayRecord) root.getReturnValue();

        assertThat(array.getLength(), is(1));
        ArrayRecord nestedArrayRecord = (ArrayRecord) array.getElements().get(0);
        ArrayRecord nested2ArrayRecord = (ArrayRecord) nestedArrayRecord.getElements().get(0);
        ArrayRecord nested3ArrayRecord = (ArrayRecord) nested2ArrayRecord.getElements().get(0);
        assertEquals(1, nested3ArrayRecord.getLength());
        assertThat(nested3ArrayRecord.getElements().get(0), isIdentity());
    }

    static class TestCase {

        public static Object[] returnArray() {
            Object[] val = new Object[1];
            val[0] = val;
            return val;
        }

        public static void main(String[] args) {
            System.out.println(returnArray());
        }
    }
}
