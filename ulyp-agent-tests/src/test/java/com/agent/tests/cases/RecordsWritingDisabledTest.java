package com.agent.tests.cases;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import org.junit.Test;

public class RecordsWritingDisabledTest extends AbstractInstrumentationTest {

    @Test
    public void shouldNotConnectToUiIfExplicitlyUiTurnedOff() {

        assertNoRecording(
                new ForkProcessBuilder()
                        .withMainClassName(X.class)
                        .withMethodToRecord("main")
                        .withOutputFile(null)
        );
    }

    static class X {

        public static void main(String[] args) {

        }
    }
}
