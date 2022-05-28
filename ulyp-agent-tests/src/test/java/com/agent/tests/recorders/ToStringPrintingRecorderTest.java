package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.PrintedObjectRecord;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.storage.CallRecord;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ToStringPrintingRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void shouldNotPrintObjectIfSettingNotSet() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnPrintableObject")
        );


        assertThat(root.getReturnValue(), instanceOf(IdentityObjectRecord.class));
    }

    @Test
    public void shouldPrintObjectIfSettingSet() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnPrintableObject")
                        .withPrintClasses("**.X")
        );


        assertThat(root.getReturnValue(), instanceOf(PrintedObjectRecord.class));

        PrintedObjectRecord printed = (PrintedObjectRecord) root.getReturnValue();
        StringObjectRecord value = printed.getPrinted();

        assertThat(value.value(), is("X{val=5}"));
    }

    @Test
    public void shouldRecordAtLeastIdentityIfToStringCallFailed() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnNonPrintableObject")
                        .withPrintClasses("**.ToStringThrowingClass")
        );


        assertThat(root.getReturnValue(), instanceOf(IdentityObjectRecord.class));

        IdentityObjectRecord printed = (IdentityObjectRecord) root.getReturnValue();
        assertThat(printed.getType().getName(), Matchers.is(ToStringThrowingClass.class.getName()));
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
            throw new RuntimeException("ToString() failed...");
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
