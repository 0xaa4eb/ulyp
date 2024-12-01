package com.agent.tests.general;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.NumberRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TypeInfoTest extends AbstractInstrumentationTest {

    @Test
    void shouldProvideArgumentTypes() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder().withMainClassName(TestCases.class)
                        .withMethodToRecord("intSum")
        );


        NumberRecord firstArg = (NumberRecord) root.getArgs().get(0);
        assertThat(firstArg.getNumberPrintedText(), is("2"));
        assertThat(firstArg.getType().getName(), is("java.util.concurrent.atomic.AtomicInteger"));

        NumberRecord secondArg = (NumberRecord) root.getArgs().get(1);

        assertThat(secondArg.getNumberPrintedText(), is("3"));
        assertThat(secondArg.getType().getName(), is("java.util.concurrent.atomic.AtomicLong"));

        ObjectRecord returnValue = root.getReturnValue();
        assertThat(returnValue.getType().getName(), is("java.lang.String"));
    }

    public static class TestCases {

        public static String intSum(AtomicInteger v1, AtomicLong v2) {
            return String.valueOf(v1.get() + v2.get());
        }

        public static void main(String[] args) {
            TestCases.intSum(new AtomicInteger(2), new AtomicLong(3));
        }
    }
}
