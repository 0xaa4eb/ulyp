package com.agent.tests.concurrent;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import org.junit.Assert;
import org.junit.Test;

public class TestTest extends AbstractInstrumentationTest {

    @Test
    public void shouldInstrumentConcurrently() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestRunner.class)
                        .withMethodToRecord(MethodMatcher.parse("**.TestRunner.main"))
        );

        Assert.assertEquals(1, recordingResult.recordings().size());
    }

    public static class TestRunner {

        public String foo(int x, int y) {
            return "ABC";
        }
        public String foo1(int x, int y) {
            return "ABC";
        }
        public String foo2(int x, int y) {
            return "ABC";
        }
        public String foo3(int x, int y) {
            return "ABC";
        }
        public String foo4(int x, int y) {
            return "ABC";
        }
        public String foo5(int x, int y) {
            return "ABC";
        }
        public String foo6(int x, int y) {
            return "ABC";
        }
        public String foo7(int x, int y) {
            return "ABC";
        }
        public String foo8(int x, int y) {
            return "ABC";
        }
        public String foo9(int x, int y) {
            return "ABC";
        }
        public String foo10(int x, int y) {
            return "ABC";
        }
        public String foo11(int x, int y) {
            return "ABC";
        }
        public String foo12(int x, int y) {
            return "ABC";
        }
        public String foo13(int x, int y) {
            return "ABC";
        }
        public String foo14(int x, int y) {
            return "ABC";
        }

        public static void main(String[] args) throws Exception {
            TestRunner testRunner = new TestRunner();
            System.out.println(testRunner.foo(1, 2));
            System.out.println(testRunner.foo1(1, 2));
            System.out.println(testRunner.foo2(1, 2));
            System.out.println(testRunner.foo3(1, 2));
            System.out.println(testRunner.foo4(1, 2));
            System.out.println(testRunner.foo5(1, 2));
            System.out.println(testRunner.foo6(1, 2));
            System.out.println(testRunner.foo7(1, 2));
            System.out.println(testRunner.foo8(1, 2));
            System.out.println(testRunner.foo9(1, 2));
            System.out.println(testRunner.foo10(1, 2));
            System.out.println(testRunner.foo11(1, 2));
            System.out.println(testRunner.foo12(1, 2));
        }
    }
}
