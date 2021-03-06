package com.test.cases;

import com.test.cases.util.TestSettingsBuilder;
import org.junit.Test;

public class RecordsWritingDisabledTest extends AbstractInstrumentationTest {

    @Test
    public void shouldNotConnectToUiIfExplicitlyUiTurnedOff() {

        assertNoRecording(
                new TestSettingsBuilder()
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
