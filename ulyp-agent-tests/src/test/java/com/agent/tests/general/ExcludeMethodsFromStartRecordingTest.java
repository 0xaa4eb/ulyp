package com.agent.tests.general;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExcludeMethodsFromStartRecordingTest extends AbstractInstrumentationTest {

    public static class A {

        public static int foo() {
            return 523423;
        }

        public static int bar() {
            return 854765;
        }

        public static void main(String[] args) {
            System.out.println(foo());
            System.out.println(bar());
        }
    }

    @Test
    void shouldRecordNormalCase() {
        RecordingResult recordingResult = runSubprocess(
            new ForkProcessBuilder()
                .withMainClassName(A.class)
                .withMethodToRecord(MethodMatcher.parse("**.A.main"))
        );

        recordingResult.assertHasRecordings();
    }

    @Test
    void shouldNotStartRecordingIfMethodExcluded() {
        RecordingResult recordingResult = runSubprocess(
            new ForkProcessBuilder()
                .withMainClassName(A.class)
                .withMethodToRecord(MethodMatcher.parse("**.A.foo,**.A.bar"))
                .withExcludeStartRecordingMethods("**.A.foo")
        );

        recordingResult.assertHasSingleRecording();
    }

    @Test
    void shouldNotStartRecordingIfMethodExcluded2() {
        RecordingResult recordingResult = runSubprocess(
            new ForkProcessBuilder()
                .withMainClassName(A.class)
                .withMethodToRecord(MethodMatcher.parse("**.A.*"))
                .withExcludeStartRecordingMethods("**.A.main")
        );

        Assertions.assertEquals(2, recordingResult.recordings().size());
    }
}
