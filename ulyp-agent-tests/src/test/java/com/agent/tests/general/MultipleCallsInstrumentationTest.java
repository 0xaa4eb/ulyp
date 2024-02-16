package com.agent.tests.general;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.storage.tree.CallRecord;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class MultipleCallsInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    public void shouldMake1000Calls() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("make1000CallsInLoop")
        );

        assertThat(root.getChildren(), hasSize(1000));
    }

    @Test
    public void shouldHaveCompleteTree() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("level0")
        );

        List<CallRecord> children = root.getChildren();

        for (CallRecord child : children) {

            Assert.assertEquals(10, child.getChildren().size());

            for (CallRecord child2 : child.getChildren()) {

                Assert.assertEquals(10, child2.getChildren().size());

                for (CallRecord child3 : child2.getChildren()) {
                    Assert.assertEquals(1000, child3.getChildren().size());
                }
            }
        }
    }

    public static class TestCase {

        private static final int calls = 1000;
        private static volatile int value;

        public static void main(String[] args) {
            new TestCase().make1000CallsLevel0();
            new TestCase().level0();
            new TestCase().make1000CallsInLoop();
        }

        public void level0() {
            for (int i = 0; i < 10; i++) {
                level1();
            }
        }

        public void level1() {
            for (int i = 0; i < 10; i++) {
                level2();
            }
        }

        public void level2() {
            for (int i = 0; i < 10; i++) {
                make1000Calls();
            }
        }

        public void make1000CallsLevel0() {
            make1000Calls();
        }

        public void make1000Calls() {
            for (int i = 0; i < calls; i++) {
                value = subCall();
            }
        }

        private int subCall() {
            return 2;
        }

        public void make1000CallsInLoop() {
            for (int i = 0; i < calls; i++) {
                value = subCall();
            }
        }
    }
}
