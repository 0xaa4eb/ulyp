package com.agent.tests.cases;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import org.junit.Test;

public class AgentDisableTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordInConcurrentMode() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(AgentDisableTestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.AgentDisableTestCase.*"))
                        .withAgentDisabled(true)
        );


        recordingResult.assertIsEmpty();
    }

    public static class AgentDisableTestCase {

        public static void main(String[] args) {
            System.out.println("Hello");
        }
    }
}
