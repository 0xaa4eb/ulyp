package com.test.recorders;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.ObjectArrayRecord;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class RecursiveArrayRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void shouldStopRecordingInfiniteRecursionAtSomeLevel() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("returnArray")
        );

        ObjectArrayRecord repr = (ObjectArrayRecord) root.getReturnValue();

        Assert.assertThat(repr.getLength(), is(1));

        ObjectArrayRecord item = (ObjectArrayRecord) repr.getRecordedItems().get(0);

        ObjectArrayRecord itemOfItem = (ObjectArrayRecord) item.getRecordedItems().get(0);

        IdentityObjectRecord itemOfItemOfItem = (IdentityObjectRecord) itemOfItem.getRecordedItems().get(0);
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
