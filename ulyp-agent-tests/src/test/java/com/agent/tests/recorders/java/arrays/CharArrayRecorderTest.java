package com.agent.tests.recorders.java.arrays;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.arrays.ByteArrayRecord;
import com.ulyp.core.recorders.arrays.CharArrayRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharArrayRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordByteArray() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnArray")
                        .withRecordArrays()
        );

        CharArrayRecord value = (CharArrayRecord) root.getReturnValue();

        assertEquals(5, value.getLength());
    }

    @Test
    void shouldRecordEmptyByteArray() {
        CallRecord root = runSubprocessAndReadFile(
            new ForkProcessBuilder()
                .withMain(TestCase.class)
                .withMethodToRecord("returnEmptyArray")
                .withRecordArrays()
        );

        ByteArrayRecord value = (ByteArrayRecord) root.getReturnValue();

        assertEquals(0, value.getLength());
    }

    public static class TestCase {

        public static void main(String[] args) {
            System.out.println(Arrays.toString(returnArray()));
            System.out.println(Arrays.toString(returnEmptyArray()));
        }

        public static char[] returnArray() {
            return new char[] {'A', 'B', 'C', 'D', 'E'};
        }

        public static byte[] returnEmptyArray() {
            return new byte[] {};
        }
    }
}
