package com.agent.tests.cases;

import com.agent.tests.cases.util.ForkProcessBuilder;
import com.agent.tests.cases.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import org.junit.Test;

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
