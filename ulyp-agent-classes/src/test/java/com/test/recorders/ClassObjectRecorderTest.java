package com.test.recorders;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.recorders.ClassObjectRecord;
import com.ulyp.storage.CallRecord;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClassObjectRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void testClassTypeReturning() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(PassClazz.class)
                        .setMethodToRecord("returnClass")
        );

        ClassObjectRecord arg = (ClassObjectRecord) root.getReturnValue();

        assertEquals(X.class.getName(), arg.getCarriedType().getName());
    }

    @Test
    public void testClassTypePassing() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(PassClazz.class)
                        .setMethodToRecord("pass")
        );

        ClassObjectRecord arg = (ClassObjectRecord) root.getArgs().get(0);

        assertEquals(X.class.getName(), arg.getCarriedType().getName());
    }

    static class X {
    }

    static class PassClazz {

        public static Class<?> returnClass() {
            return X.class;
        }

        public static void pass(Class<?> clazz) {
            System.out.println(clazz);
        }

        public static void main(String[] args) {
            pass(X.class);
            System.out.println(returnClass());
        }
    }
}
