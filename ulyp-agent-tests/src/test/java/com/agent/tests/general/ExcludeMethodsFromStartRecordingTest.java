package com.agent.tests.general;

import org.junit.Assert;
import org.junit.Test;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;

public class ExcludeMethodsFromStartRecordingTest extends AbstractInstrumentationTest {

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
    public void shouldRecordNormalCase() {
        RecordingResult recordingResult = runSubprocess(
            new ForkProcessBuilder()
                .withMainClassName(A.class)
                .withMethodToRecord(MethodMatcher.parse("**.A.main"))
        );

        recordingResult.assertHasRecordings();
    }

    @Test
    public void shouldNotStartRecordingIfMethodExcluded() {
        RecordingResult recordingResult = runSubprocess(
            new ForkProcessBuilder()
                .withMainClassName(A.class)
                .withMethodToRecord(MethodMatcher.parse("**.A.foo,**.A.bar"))
                .withExcludeStartRecordingMethods("**.A.foo")
        );

        recordingResult.assertHasSingleRecording();
    }

    @Test
    public void shouldNotStartRecordingIfMethodExcluded2() {
        RecordingResult recordingResult = runSubprocess(
            new ForkProcessBuilder()
                .withMainClassName(A.class)
                .withMethodToRecord(MethodMatcher.parse("**.A.*"))
                .withExcludeStartRecordingMethods("**.A.main")
        );

        Assert.assertEquals(2, recordingResult.recordings().size());
    }
}
