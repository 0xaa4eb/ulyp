package com.agent.tests.general;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.SystemProp;
import com.ulyp.storage.CallRecord;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AggressiveRecordingTest extends AbstractInstrumentationTest {

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
    public void testAggresiveRecording() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withSystemProp(SystemProp.builder().key("ulyp.aggressive").build())
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("main")
        );

        assertThat(root.getChildren(), Matchers.hasSize(5));

        assertThat(root.getChildren().get(0).getMethod().getName(), is("<clinit>"));
        assertThat(root.getChildren().get(0).getMethod().getDeclaringType().getName(), is("com.agent.tests.general.AggressiveRecordingTest$X"));
        assertThat(root.getChildren().get(1).getMethod().getDeclaringType().getName(), is("com.agent.tests.general.AggressiveRecordingTest$X"));
        assertThat(root.getChildren().get(1).getMethod().getName(), is("<init>"));
        assertThat(root.getChildren().get(2).getMethod().getName(), is("<clinit>"));
        assertThat(root.getChildren().get(2).getMethod().getDeclaringType().getName(), containsString("com.agent.tests.general.AggressiveRecordingTest$TestCase$$Lambda$ByteBuddy"));
        assertThat(root.getChildren().get(3).getMethod().getName(), is("<init>"));
        assertThat(root.getChildren().get(4).getMethod().getName(), is("run"));
    }
}
