package com.test.printers;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.printers.IdentityObjectRepresentation;
import com.ulyp.core.printers.NumberObjectRepresentation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IdentityObjectPrintingTest extends AbstractInstrumentationTest {

    @Test
    public void testIdentityRepresentation() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("pass")
        );

        NumberObjectRepresentation objectRepresentation = (NumberObjectRepresentation) root.getReturnValue();

        int hashCode = Integer.parseInt(objectRepresentation.getNumberPrintedText());

        IdentityObjectRepresentation arg = (IdentityObjectRepresentation) root.getArgs().get(0);

        assertEquals(hashCode, arg.getHashCode());
        assertEquals(X.class.getName(), arg.getType().getName());
    }


    static class X {

    }

    static class TestCase {

        public static int pass(X x) {
            return System.identityHashCode(x);
        }

        public static void main(String[] args) {
            pass(new X());
        }
    }
}
