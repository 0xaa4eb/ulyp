package com.agent.tests.recorders;

import java.util.List;
import java.util.function.Supplier;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.storage.CallRecord;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class LambdaRecordTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordLambdaCall() {
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
