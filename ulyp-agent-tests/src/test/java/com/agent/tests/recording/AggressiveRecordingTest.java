package com.agent.tests.recording;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.SystemProp;
import com.ulyp.storage.tree.CallRecord;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class AggressiveRecordingTest extends AbstractInstrumentationTest {

    public static class X {

        public X() {
        }
    }

    public static class TestCase {

        public static void main(String[] args) {
            System.out.println(new X());
            Runnable r = () -> {
                System.out.println("42");
            };
            r.run();
        }
    }

    @Test
    void testAggresiveRecording() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withSystemProp(SystemProp.builder().key("ulyp.aggressive").build())
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("main")
        );

        assertThat(root.getChildren(), Matchers.hasSize(5));

        assertThat(root.getChildren().get(0).getMethod().getName(), is("<clinit>"));
        assertThat(root.getChildren().get(0).getMethod().getDeclaringType().getName(), is("com.agent.tests.recording.AggressiveRecordingTest$X"));
        assertThat(root.getChildren().get(1).getMethod().getDeclaringType().getName(), is("com.agent.tests.recording.AggressiveRecordingTest$X"));
        assertThat(root.getChildren().get(1).getMethod().getName(), is("<init>"));
        assertThat(root.getChildren().get(2).getMethod().getName(), is("<clinit>"));
        assertThat(root.getChildren().get(2).getMethod().getDeclaringType().getName(), containsString("com.agent.tests.recording.AggressiveRecordingTest$TestCase$$Lambda$ByteBuddy"));
        assertThat(root.getChildren().get(3).getMethod().getName(), is("<init>"));
        assertThat(root.getChildren().get(4).getMethod().getName(), is("run"));
    }
}
