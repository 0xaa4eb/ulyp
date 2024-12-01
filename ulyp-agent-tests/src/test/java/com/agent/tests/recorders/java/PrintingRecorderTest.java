package com.agent.tests.recorders.java;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.PrintedObjectRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

class PrintingRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldNotPrintObjectIfSettingNotSet() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnPrintableObject")
        );


        assertThat(root.getReturnValue(), instanceOf(IdentityObjectRecord.class));
    }

    @Test
    void shouldPrintObjectIfSettingSet() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnPrintableObject")
                        .withPrintTypes("**.X")
        );


        assertThat(root.getReturnValue(), instanceOf(PrintedObjectRecord.class));

        PrintedObjectRecord printed = (PrintedObjectRecord) root.getReturnValue();
        String value = printed.getPrintedObject();

        assertThat(value, is("X{val=5}"));
    }

    @Test
    void shouldRecordAtLeastIdentityIfToStringCallFailed() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnNonPrintableObject")
                        .withPrintTypes("**.ToStringThrowingClass")
        );


        assertThat(root.getReturnValue(), instanceOf(IdentityObjectRecord.class));

        IdentityObjectRecord printed = (IdentityObjectRecord) root.getReturnValue();
        assertThat(printed.getType().getName(), is(ToStringThrowingClass.class.getName()));
    }

    static class X {
        private final int val;

        X(int val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return "X{" +
                    "val=" + val +
                    '}';
        }
    }

    static class ToStringThrowingClass {
        @Override
        public String toString() {
            throw new RuntimeException("not supported");
        }
    }

    static class TestCase {

        public static X returnPrintableObject() {
            return new X(5);
        }

        public static ToStringThrowingClass returnNonPrintableObject() {
            return new ToStringThrowingClass();
        }

        public static void main(String[] args) {
            System.out.println(returnPrintableObject());

            // Do not call toString() as it will throw exception
            System.out.println(returnNonPrintableObject().hashCode());
        }
    }
}
