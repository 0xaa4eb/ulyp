package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.BooleanRecord;
import com.ulyp.core.recorders.ClassObjectRecord;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.storage.CallRecord;
import org.junit.Assert;
import org.junit.Test;

public class DynamicRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void shouldUseBooleanRecorderIfBooleanIsPassed() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("passBoolean")
        );

        BooleanRecord repr = (BooleanRecord) root.getArgs().get(0);

        Assert.assertTrue(repr.getValue());
    }

    @Test
    public void shouldUseStringRecorderIfBooleanIsPassed() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("passString")
        );

        StringObjectRecord objectRepresentation = (StringObjectRecord) root.getArgs().get(0);

        Assert.assertEquals("ABC", objectRepresentation.value());
    }

    @Test
    public void shouldUseClassRecorderIfClassIsPassed() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("passClass")
        );

        ClassObjectRecord record = (ClassObjectRecord) root.getArgs().get(0);
    }

    static class TestCase {

        public static void passBoolean(Object val) {
            System.out.println(val);
        }

        public static void passString(Object val) {
            System.out.println(val);
        }

        public static void passClass(Object val) {
            System.out.println(val);
        }

        public static void main(String[] args) {
            passBoolean(true);
            passString("ABC");
            passClass(int[].class);
        }
    }
}
