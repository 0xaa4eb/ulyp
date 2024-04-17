package com.agent.tests.recorders;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.storage.tree.CallRecord;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.hasSize;

class LambdaRecordTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordLambdaCall() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("invoke")
                        .withInstrumentLambdas(true)
        );

        List<CallRecord> children = root.getChildren();

        assertThat(children, hasSize(1));
    }

    public static class TestCase {

        public static Integer invoke(Supplier<Integer> supplier) {
            return supplier.get();
        }

        public static void main(String[] args) {
            Supplier<Integer> supplier = () -> 42;
            invoke(supplier);
        }
    }
}
