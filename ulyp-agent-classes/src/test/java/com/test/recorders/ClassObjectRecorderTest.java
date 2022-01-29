package com.test.recorders;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.recorders.ClassObjectRecord;
import com.ulyp.storage.CallRecord;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ClassObjectRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void testClassTypeReturning() {

        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(PassClazz.class)
                        .setMethodToRecord("returnClass")
        );

        ClassObjectRecord arg = (ClassObjectRecord) root.getReturnValue();

        assertThat(arg.getCarriedType().getName(), is(X.class.getName()));
    }

    @Test
    public void testClassTypePassing() {

        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(PassClazz.class)
                        .setMethodToRecord("takeClass")
        );

        ClassObjectRecord arg = (ClassObjectRecord) root.getArgs().get(0);

        assertThat(arg.getCarriedType().getName(), is(X.class.getName()));
    }

    static class X {
    }

    static class PassClazz {

        public static Class<?> returnClass() {
            return X.class;
        }

        public static void takeClass(Class<?> clazz) {
            System.out.println(clazz);
        }

        public static void main(String[] args) {
            takeClass(X.class);
            System.out.println(returnClass());
        }
    }
}
