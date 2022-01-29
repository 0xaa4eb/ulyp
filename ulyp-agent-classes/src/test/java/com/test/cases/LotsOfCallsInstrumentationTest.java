package com.test.cases;

import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.storage.CallRecord;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class LotsOfCallsInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    public void shouldMake1000Calls() {
        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(LotsOfCallsTestCases.class)
                        .setMethodToRecord("make1000CallsSep")
        );

        assertThat(root.getChildren(), hasSize(1000));
    }

    @Test
    public void shouldHaveCompleteTree() {
        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(LotsOfCallsTestCases.class)
                        .setMethodToRecord("level0")
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

    public static class LotsOfCallsTestCases {

        private static volatile int calls = 1000;
        private static volatile int value;

        public static void main(String[] args) {
            SafeCaller.call(() -> new LotsOfCallsTestCases().make1000CallsLevel0());
            SafeCaller.call(() -> new LotsOfCallsTestCases().level0());
            SafeCaller.call(() -> new LotsOfCallsTestCases().make1000CallsSep());
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

        public void make1000CallsSep() {
            for (int i = 0; i < calls; i++) {
                value = subCall();
            }
        }
    }
}
