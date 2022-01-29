package com.test.cases;

import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.storage.CallRecord;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RecursionInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    public void testFibonacciMethodCall() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(RecursionTestCases.class)
                        .setMethodToRecord("fibonacci")
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
