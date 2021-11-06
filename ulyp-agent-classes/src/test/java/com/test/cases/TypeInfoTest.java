package com.test.cases;

import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.printers.NumberRecord;
import com.ulyp.core.printers.ObjectRecord;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TypeInfoTest extends AbstractInstrumentationTest {

    @Test
    public void shouldProvideArgumentTypes() {
        CallRecord root = runForkWithUi(
                new ForkProcessBuilder().setMainClassName(TestCases.class)
                        .setMethodToRecord("intSum")
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
            SafeCaller.call(() -> TestCases.intSum(new AtomicInteger(2), new AtomicLong(3)));
        }
    }
}
