package com.agent.tests.general;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class RecursionInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    void testFibonacciMethodCall() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(RecursionTestCases.class)
                        .withMethodToRecord("fibonacci")
        );

        assertThat(root.getSubtreeSize(), is(177));
    }

    public static class RecursionTestCases {

        public static void main(String[] args) {
            System.out.println(new RecursionTestCases().fibonacci(10));
        }

        public int fibonacci(int v) {
            if (v <= 1) {
                return v;
            }
            return fibonacci(v - 1) + fibonacci(v - 2);
        }
    }
}
