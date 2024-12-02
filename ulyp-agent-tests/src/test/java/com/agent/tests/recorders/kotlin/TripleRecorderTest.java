package com.agent.tests.recorders.kotlin;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.kotlin.KtTripleRecord;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static com.agent.tests.util.RecordingMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class TripleRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordTriple() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.BasicTypesTestKt.getTriple"))
        );

        KtTripleRecord pair = (KtTripleRecord) root.getReturnValue();
        assertThat(pair.getFirst(), isString("ABC"));
        assertThat(pair.getSecond(), isIntegral(42));
        assertThat(pair.getThird(), isString("ZXVZC"));
    }

    @Test
    void shouldRecordTriple2() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.BasicTypesTestKt.getTriple2"))
        );

        KtTripleRecord pair = (KtTripleRecord) root.getReturnValue();
        assertThat(pair.getThird(), isNull());
    }

    static class TestCase {

        public static void main(String[] args) {
            System.out.println(BasicTypesTestKt.getTriple());
            System.out.println(BasicTypesTestKt.getTriple2());
        }
    }
}
