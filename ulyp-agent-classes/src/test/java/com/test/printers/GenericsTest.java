package com.test.printers;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.TestSettingsBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.printers.StringObjectRepresentation;
import com.ulyp.core.util.MethodMatcher;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GenericsTest extends AbstractInstrumentationTest {

    @Test
    public void testAtomicIntegerSum() {

        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(X.class)
                        .setMethodToRecord(MethodMatcher.parse("Box.get"))
        );

        StringObjectRepresentation returnValue = (StringObjectRepresentation) root.getReturnValue();

        assertThat(returnValue.getValue(), is("abc"));
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
