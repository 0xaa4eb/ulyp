package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GenericsRecorderTest extends AbstractInstrumentationTest {

    @Test
    void testAtomicIntegerSum() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(X.class)
                        .withMethodToRecord(MethodMatcher.parse("**.Box.get"))
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
