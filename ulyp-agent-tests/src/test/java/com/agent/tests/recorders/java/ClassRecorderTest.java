package com.agent.tests.recorders.java;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.ClassRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ClassRecorderTest extends AbstractInstrumentationTest {

    @Test
    void testClassTypeReturning() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(PassClazz.class)
                        .withMethodToRecord("returnClass")
        );

        ClassRecord arg = (ClassRecord) root.getReturnValue();

        assertThat(arg.getDeclaringType().getName(), is(X.class.getName()));
    }

    @Test
    void testClassTypePassing() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(PassClazz.class)
                        .withMethodToRecord("takeClass")
        );

        ClassRecord arg = (ClassRecord) root.getArgs().get(0);

        assertThat(arg.getDeclaringType().getName(), is(X.class.getName()));
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
