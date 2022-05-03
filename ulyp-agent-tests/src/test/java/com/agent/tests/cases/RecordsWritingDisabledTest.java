package com.agent.tests.cases;

import com.agent.tests.cases.util.ForkProcessBuilder;
import org.junit.Test;

public class RecordsWritingDisabledTest extends AbstractInstrumentationTest {

    @Test
    public void shouldNotConnectToUiIfExplicitlyUiTurnedOff() {

        assertNoRecording(
                new ForkProcessBuilder()
                        .setMainClassName(X.class)
                        .setMethodToRecord("main")
                        .setOutputFile(null)
        );
    }

    static class X {

        public static void main(String[] args) {

        }
    }
}
