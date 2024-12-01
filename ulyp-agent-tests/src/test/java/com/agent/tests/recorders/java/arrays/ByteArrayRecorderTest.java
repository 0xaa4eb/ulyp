package com.agent.tests.recorders.java.arrays;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.arrays.ByteArrayRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ByteArrayRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordByteArray() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnByteArray")
                        .withRecordArrays()
        );

        ByteArrayRecord value = (ByteArrayRecord) root.getReturnValue();

        assertEquals(4, value.getLength());
    }

    @Test
    void shouldRecordEmptyByteArray() {
        CallRecord root = runSubprocessAndReadFile(
            new ForkProcessBuilder()
                .withMainClassName(TestCase.class)
                .withMethodToRecord("returnEmptyArray")
                .withRecordArrays()
        );

        ByteArrayRecord value = (ByteArrayRecord) root.getReturnValue();

        assertEquals(0, value.getLength());
    }

    public static class TestCase {

        public static void main(String[] args) {
            System.out.println(Arrays.toString(returnByteArray()));
            System.out.println(Arrays.toString(returnEmptyArray()));
        }

        public static byte[] returnByteArray() {
            return new byte[] {1, 2, 3, 4};
        }

        public static byte[] returnEmptyArray() {
            return new byte[] {};
        }
    }
}
