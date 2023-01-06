package com.agent.tests.general;

import com.agent.tests.util.AbstractInstrumentationTest;

public class StackTraceRecordingTest extends AbstractInstrumentationTest {

    public static class X {

        public void bar() {

        }

        public void foo() {
            bar();
        }
    }

    public static class StackTraceTestCase {

        public static void main(String[] args) {
            new X().foo();
        }
    }
}
