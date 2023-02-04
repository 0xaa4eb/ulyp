package com.agent.tests.recorders;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.arrays.ByteArrayRecord;
import com.ulyp.storage.CallRecord;

public class ByteArrayRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordByteArray() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnByteArray")
        );

        ByteArrayRecord value = (ByteArrayRecord) root.getReturnValue();

        Assert.assertEquals(4, value.getLength());
    }

    @Test
    public void shouldRecordEmptyByteArray() {
        CallRecord root = runSubprocessAndReadFile(
            new ForkProcessBuilder()
                .withMainClassName(TestCase.class)
                .withMethodToRecord("returnEmptyArray")
        );

        ByteArrayRecord value = (ByteArrayRecord) root.getReturnValue();

        Assert.assertEquals(0, value.getLength());
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
