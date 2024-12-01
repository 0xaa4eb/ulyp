package com.agent.tests.recorders.kotlin;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.kotlin.KtPairRecord;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static com.agent.tests.util.RecordingMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class PairRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordPair() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.BasicTypesTestKt.getPair"))
        );

        KtPairRecord pair = (KtPairRecord) root.getReturnValue();
        assertThat(pair.getFirst(), isString("ABC"));
        assertThat(pair.getSecond(), isIntegral(42));
    }

    @Test
    void shouldRecordPair2() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.BasicTypesTestKt.getPair2"))
        );

        KtPairRecord pair = (KtPairRecord) root.getReturnValue();
        assertThat(pair.getFirst(), isString("ABC"));
        assertThat(pair.getSecond(), isNull());
    }

    static class TestCase {

        public static void main(String[] args) {
            System.out.println(BasicTypesTestKt.getPair());
            System.out.println(BasicTypesTestKt.getPair2());
        }
    }
}
