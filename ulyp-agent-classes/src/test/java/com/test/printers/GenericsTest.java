package com.test.printers;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.core.util.MethodMatcher;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GenericsTest extends AbstractInstrumentationTest {

    @Test
    public void testAtomicIntegerSum() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(X.class)
                        .setMethodToRecord(MethodMatcher.parse("Box.get"))
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
