package com.agent.tests.recording;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.Recording;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ThreadNameRecordingTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordAllMethodsIfThreadsPropNotSpecified() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.Service.*"))
        );

        List<Recording> recordings = recordingResult.recordings();

        assertEquals(recordings.size(), 2);
    }

    @Test
    public void shouldRecordAllMethodsIfThreadsPropMatchesAllThreads() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.Service.*"))
                        .withRecordThreads("J.*")
        );

        List<Recording> recordings = recordingResult.recordings();

        assertEquals(recordings.size(), 2);
    }

    @Test
    public void shouldRecordOnlyOneMethodWithSpecifiedThreadName() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.Service.*"))
                        .withRecordThreads("John")
        );

        List<Recording> recordings = recordingResult.recordings();

        assertEquals(recordings.size(), 1);
    }

    @Test
    public void shouldNotStartRecordingIfThreadNameDoesntMatch() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.Service.*"))
                        .withRecordThreads("MXNCMZXC")
        );

        List<Recording> recordings = recordingResult.recordings();

        assertEquals(recordings.size(), 0);
    }

    public static class Service {
        public void foo() {
            System.out.println(Thread.currentThread().getName());
        }

        public void bar() {
            System.out.println(Thread.currentThread().getName());
        }
    }

    public static class TestCase {

        public static void main(String[] args) throws InterruptedException {
            Service service = new Service();

            Thread t1 = new Thread(service::foo);
            t1.setName("John");
            t1.start();
            t1.join();

            Thread t2 = new Thread(service::bar);
            t2.setName("Jack");
            t2.start();
            t2.join();
        }
    }
}
