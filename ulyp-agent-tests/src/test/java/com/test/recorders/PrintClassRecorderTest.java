package com.test.recorders;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.PrintedObjectRecord;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.storage.CallRecord;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class PrintClassRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void shouldNotPrintObjectIfSettingNotSet() {

        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("foo")
        );


        assertThat(root.getReturnValue(), instanceOf(IdentityObjectRecord.class));
    }

    @Test
    public void shouldPrintObjectIfSettingSet() {

        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("foo")
                        .setPrintClasses("**.X")
        );


        assertThat(root.getReturnValue(), instanceOf(PrintedObjectRecord.class));

        PrintedObjectRecord printed = (PrintedObjectRecord) root.getReturnValue();
        StringObjectRecord value = printed.getPrinted();

        assertThat(value.value(), is("X{val=5}"));
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

    static class TestCase {

        public static X foo() {
            return new X(5);
        }

        public static void main(String[] args) {
            System.out.println(foo());
        }
    }
}
