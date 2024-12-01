package com.agent.tests.recorders.java;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.arrays.ArrayRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class RecursiveArrayRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldStopRecordingInfiniteRecursionAtSomeLevel() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnArray")
                        .withRecordArrays()
        );

        ArrayRecord repr = (ArrayRecord) root.getReturnValue();

        assertThat(repr.getLength(), is(1));

        ArrayRecord item = (ArrayRecord) repr.getElements().get(0);

        ArrayRecord itemOfItem = (ArrayRecord) item.getElements().get(0);

        ArrayRecord itemOfItemOfItem = (ArrayRecord) itemOfItem.getElements().get(0);
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
