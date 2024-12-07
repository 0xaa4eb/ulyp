package com.agent.tests.recorders.java;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.basic.ThreadRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ThreadRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordThread() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnThread")
        );

        ThreadRecord value = (ThreadRecord) root.getReturnValue();

        Assertions.assertEquals("name-123", value.getName());
    }

    public static class TestCase {

        public static void main(String[] args) {
            System.out.println(returnThread());
        }

        public static Thread returnThread() {
            Thread th = new Thread(() -> {});
            th.setName("name-123");
            th.setDaemon(true);
            return th;
        }
    }
}
