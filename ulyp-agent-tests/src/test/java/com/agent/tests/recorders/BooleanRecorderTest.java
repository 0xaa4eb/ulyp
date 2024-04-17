package com.agent.tests.recorders;

import org.junit.jupiter.api.Test;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.BooleanRecord;
import com.ulyp.storage.tree.CallRecord;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordBoxedBooleanTrue() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnBoxedBooleanTrue")
        );

        BooleanRecord value = (BooleanRecord) root.getReturnValue();

        assertTrue(value.getValue());
    }

    @Test
    void shouldRecordBoxedBooleanFalse() {
        CallRecord root = runSubprocessAndReadFile(
            new ForkProcessBuilder()
                .withMainClassName(TestCase.class)
                .withMethodToRecord("returnBoxedBooleanFalse")
        );

        BooleanRecord value = (BooleanRecord) root.getReturnValue();

        assertFalse(value.getValue());
    }

    @Test
    void shouldRecordPrimitiveBooleanTrue() {
        CallRecord root = runSubprocessAndReadFile(
            new ForkProcessBuilder()
                .withMainClassName(TestCase.class)
                .withMethodToRecord("returnPrimitiveBooleanTrue")
        );

        BooleanRecord value = (BooleanRecord) root.getReturnValue();

        assertTrue(value.getValue());
    }

    @Test
    void shouldRecordPrimitiveBooleanFalse() {
        CallRecord root = runSubprocessAndReadFile(
            new ForkProcessBuilder()
                .withMainClassName(TestCase.class)
                .withMethodToRecord("returnPrimitiveBooleanFalse")
        );

        BooleanRecord value = (BooleanRecord) root.getReturnValue();

        assertFalse(value.getValue());
    }

    public static class TestCase {

        public static void main(String[] args) {
            System.out.println(returnBoxedBooleanTrue().getClass());
            System.out.println(returnBoxedBooleanFalse().getClass());
            System.out.println(returnPrimitiveBooleanTrue());
            System.out.println(returnPrimitiveBooleanFalse());
        }

        public static Boolean returnBoxedBooleanTrue() {
            return true;
        }

        public static Boolean returnBoxedBooleanFalse() {
            return false;
        }

        public static boolean returnPrimitiveBooleanTrue() {
            return true;
        }

        public static boolean returnPrimitiveBooleanFalse() {
            return false;
        }
    }
}
