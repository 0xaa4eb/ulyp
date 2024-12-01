package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.BooleanRecord;
import com.ulyp.core.recorders.ClassRecord;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldUseBooleanRecorderIfBooleanIsPassed() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("passBoolean")
        );

        BooleanRecord repr = (BooleanRecord) root.getArgs().get(0);

        assertTrue(repr.getValue());
    }

    @Test
    void shouldUseStringRecorderIfBooleanIsPassed() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("passString")
        );

        StringObjectRecord objectRepresentation = (StringObjectRecord) root.getArgs().get(0);

        assertEquals("ABC", objectRepresentation.value());
    }

    @Test
    void shouldUseClassRecorderIfClassIsPassed() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("passClass")
        );

        assertThat(root.getArgs().get(0), CoreMatchers.instanceOf(ClassRecord.class));
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
