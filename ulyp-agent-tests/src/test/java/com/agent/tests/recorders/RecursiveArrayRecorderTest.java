package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.arrays.ObjectArrayRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class RecursiveArrayRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void shouldStopRecordingInfiniteRecursionAtSomeLevel() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnArray")
        );

        ObjectArrayRecord repr = (ObjectArrayRecord) root.getReturnValue();

        Assert.assertThat(repr.getLength(), is(1));

        ObjectArrayRecord item = (ObjectArrayRecord) repr.getRecordedItems().get(0);

        ObjectArrayRecord itemOfItem = (ObjectArrayRecord) item.getRecordedItems().get(0);

        ObjectArrayRecord itemOfItemOfItem = (ObjectArrayRecord) itemOfItem.getRecordedItems().get(0);
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
