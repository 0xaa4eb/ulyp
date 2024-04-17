package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.NumberRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdentityRecorderTest extends AbstractInstrumentationTest {

    @Test
    void testIdentityRepresentation() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("pass")
        );

        NumberRecord objectRepresentation = (NumberRecord) root.getReturnValue();

        int hashCode = Integer.parseInt(objectRepresentation.getNumberPrintedText());

        IdentityObjectRecord arg = (IdentityObjectRecord) root.getArgs().get(0);

        assertEquals(hashCode, arg.getHashCode());
        assertEquals(X.class.getName(), arg.getType().getName());
    }

    @Test
    void testIdentityRepresentationOfCallee() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("main")
        );

        CallRecord fooCall = root.getChildren().get(1);

        ObjectRecord callee = fooCall.getCallee();
        assertEquals(X.class.getName(), callee.getType().getName());
    }


    static class X {
        public int foo() {
            return 5;
        }
    }

    static class TestCase {

        public static int pass(X x) {
            return System.identityHashCode(x);
        }

        public static void main(String[] args) {
            pass(new X());
            System.out.println(new X().foo());
        }
    }
}
