package com.agent.tests.recorders.java;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.basic.CharRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordFileObject() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnChar")
        );

        CharRecord value = (CharRecord) root.getReturnValue();

        assertEquals('A', value.getValue());
    }

    public static class TestCase {

        public static void main(String[] args) {
            System.out.println(returnChar());
        }

        public static char returnChar() {
            return 'A';
        }
    }
}
