package com.test.cases;

import com.test.cases.util.ForkProcessBuilder;
import com.test.cases.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AgentDisableTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordInConcurrentMode() {
        RecordingResult recordingResult = runForkProcess(
                new ForkProcessBuilder()
                        .setMainClassName(AgentDisableTestCase.class)
                        .setMethodToRecord(MethodMatcher.parse("**.AgentDisableTestCase.*"))
                        .setAgentDisabled(true)
        );


        recordingResult.assertIsEmpty();
    }

    public static class AgentDisableTestCase {

        public static void main(String[] args) throws InterruptedException {
            System.out.println("Hello");
        }
    }
}
