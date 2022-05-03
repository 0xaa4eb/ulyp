package com.agent.tests.recorders;

import com.agent.tests.cases.AbstractInstrumentationTest;
import com.agent.tests.cases.util.ForkProcessBuilder;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.CallRecord;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GenericsRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void testAtomicIntegerSum() {

        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(X.class)
                        .setMethodToRecord(MethodMatcher.parse("**.Box.get"))
        );

        StringObjectRecord returnValue = (StringObjectRecord) root.getReturnValue();

        assertThat(returnValue.value(), is("abc"));
    }

    static class Box<T> {

        private final T val;

        Box(T val) {
            this.val = val;
        }

        T get() {
            return this.val;
        }
    }

    static class X {
        public static void main(String[] args) {
            System.out.println(new Box<>("abc").get());
        }
    }
}
