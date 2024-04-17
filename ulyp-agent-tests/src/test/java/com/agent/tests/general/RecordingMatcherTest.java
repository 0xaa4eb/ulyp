package com.agent.tests.general;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.hasSize;

class RecordingMatcherTest extends AbstractInstrumentationTest {

    @Test
    void shouldNotRecordWithInvalidMatcher() {

        assertThat(runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCases.class)
                        .withMethodToRecord(MethodMatcher.parse("**.TestCasesAZASdasd.main"))
        ).recordings(), Matchers.empty());

        assertThat(runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCases.class)
                        .withMethodToRecord(MethodMatcher.parse("a.b.c.RecordingMatcherTest.TestCases.main"))
        ).recordings(), Matchers.empty());
    }

    @Test
    void shouldRecordMainMethodIfMatcherIsNotSpecified() {

        assertThat(runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCases.class)
                        .withMethodToRecord(MethodMatcher.parse("*.*"))
        ).recordings(), hasSize(1));

        assertThat(runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCases.class)
                        .withMethodToRecord(MethodMatcher.parse("**.TestCases.main"))
        ).recordings(), hasSize(1));

        assertThat(runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCases.class)
                        .withMethodToRecord(MethodMatcher.parse("com.agent.tests.general.RecordingMatcherTest.TestCases.main"))
        ).recordings(), hasSize(1));

        assertThat(runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCases.class)
                        .withMethodToRecord(MethodMatcher.parse("**.RecordingMatcherTest.TestCases.main"))
        ).recordings(), hasSize(1));
    }

    @Test
    // Not yet supported with default methods
    void shouldBeAbleToMatchInterfaceMethodUsingImplementingClassName() {

        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCases.class)
                        .withMethodToRecord(MethodMatcher.parse("**.Clazz.bar"))
        );

        assertThat(recordingResult.recordings(), hasSize(1));
    }

    @Test
    @Disabled
    // Not yet supported with default methods
    void shouldBeAbleToMatchDefaultMethod() {

        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCases.class)
                        .withMethodToRecord(MethodMatcher.parse("**.Clazz.foo"))
        );

        assertThat(recordingResult.recordings(), hasSize(3));
    }

    @Test
    void testRecordViaInterfaceMatcher() {
        assertThat(runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCases.class)
                        .withMethodToRecord(MethodMatcher.parse("**.Interface.foo"))
        ).recordings(), hasSize(1));
    }

    @Test
    void shouldRecordAllMethods() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(MultithreadedExample.class)
                        .withMethodToRecord(MethodMatcher.parse("*.*"))
        );

        // threads have two recording sessions each (constructor + method call)
        recordingResult.assertRecordingSessionCount(3);
    }

    public interface Interface {

        default int foo() {
            return 42;
        }

        int bar();

        default int zoo() {
            return 2;
        }
    }

    public static class MultithreadedExample {

        public static void main(String[] args) throws InterruptedException {
            Thread t1 = new Thread(() -> new Clazz().bar());
            t1.start();

            Thread t2 = new Thread(() -> new Clazz().zoo());
            t2.start();

            t1.join();
            t2.join();

            System.out.println(new Clazz().foo());
        }
    }

    public static class Clazz implements Interface {

        @Override
        public int bar() {
            return 55;
        }
    }

    public static class TestCases {

        public static void main(String[] args) {
            System.out.println(new Clazz().foo());
            System.out.println(new Clazz().bar());
            System.out.println(new Clazz().zoo());
        }
    }
}
