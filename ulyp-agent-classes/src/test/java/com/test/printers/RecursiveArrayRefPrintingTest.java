package com.test.printers;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.printers.IdentityObjectRepresentation;
import com.ulyp.core.printers.ObjectArrayRepresentation;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class RecursiveArrayRefPrintingTest extends AbstractInstrumentationTest {

    @Test
    public void shouldStopRecordingInfiniteRecursionAtSomeLevel() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("returnArray")
        );

        ObjectArrayRepresentation repr = (ObjectArrayRepresentation) root.getReturnValue();

        Assert.assertThat(repr.getLength(), is(1));

        ObjectArrayRepresentation item = (ObjectArrayRepresentation) repr.getRecordedItems().get(0);

        ObjectArrayRepresentation itemOfItem = (ObjectArrayRepresentation) item.getRecordedItems().get(0);

        IdentityObjectRepresentation itemOfItemOfItem = (IdentityObjectRepresentation) itemOfItem.getRecordedItems().get(0);
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
