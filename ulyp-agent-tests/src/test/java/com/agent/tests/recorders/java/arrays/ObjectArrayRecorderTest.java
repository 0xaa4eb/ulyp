package com.agent.tests.recorders.java.arrays;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingMatchers;
import com.ulyp.core.recorders.*;
import com.ulyp.core.recorders.arrays.ArrayRecord;
import com.ulyp.core.recorders.basic.ClassRecord;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.agent.tests.util.RecordingMatchers.isIdentity;
import static com.agent.tests.util.RecordingMatchers.isString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ObjectArrayRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldNotRecordArrayValuesByDefault() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TakesEmptyObjectArray.class)
                        .withMethodToRecord("accept")
        );


        IdentityObjectRecord record = (IdentityObjectRecord) root.getArgs().get(0);


        assertThat(record.getType().getName(), is("[Ljava.lang.Object;"));
    }

    @Test
    void shouldProvideArgumentTypes() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TakesEmptyObjectArray.class)
                        .withMethodToRecord("accept")
                        .withRecordArrays()
        );


        ArrayRecord record = (ArrayRecord) root.getArgs().get(0);


        assertThat(record.getLength(), is(0));
        assertThat(record.getElements(), Matchers.empty());
    }

    @Test
    void shouldRecordSimpleArrayWithString() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TakesStringArrayWithSomeString.class)
                        .withMethodToRecord("accept")
                        .withRecordArrays()
        );


        ArrayRecord record = (ArrayRecord) root.getArgs().get(0);


        assertThat(record.getLength(), is(6));

        List<? extends ObjectRecord> elements = record.getElements();

        assertThat(elements, Matchers.hasSize(3));
        assertThat(elements.get(0), isString("A"));
        assertThat(elements.get(1), isString("B"));
        assertThat(elements.get(2), isString("C"));
    }

    @Test
    void shouldRecordSimpleArrayWithStringWithCustomMaxCountSpecified() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TakesStringArrayWithSomeString.class)
                        .withMethodToRecord("accept")
                        .withRecordArrays()
                        .withRecordArrayItems(5)
        );


        ArrayRecord record = (ArrayRecord) root.getArgs().get(0);


        assertThat(record.getLength(), is(6));

        List<? extends ObjectRecord> elements = record.getElements();

        assertThat(elements, Matchers.hasSize(5));
        assertThat(elements.get(0), isString("A"));
        assertThat(elements.get(1), isString("B"));
        assertThat(elements.get(2), isString("C"));
        assertThat(elements.get(3), isString("D"));
        assertThat(elements.get(4), isString("E"));
    }

    @Test
    void testUserDefinedClassArrayWith3Elements() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TakesVariousItemsArray.class)
                        .withMethodToRecord("accept")
                        .withRecordArrays()
        );


        ArrayRecord record = (ArrayRecord) root.getArgs().get(0);

        assertThat(record.getLength(), is(5));

        List<? extends ObjectRecord> elements = record.getElements();

        assertThat(elements.get(0), isIdentity(X.class.getName()));
        assertThat(elements.get(1), RecordingMatchers.isIntegral(664));
        ClassRecord arg4 = (ClassRecord) elements.get(2);
        assertThat(arg4.getDeclaringType().getName(), is(Object.class.getName()));
    }

    @Test
    void testVarargs() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(VaragsTestCase.class)
                        .withMethodToRecord("takeVararg")
                        .withRecordArrays()
        );


        ArrayRecord record = (ArrayRecord) root.getArgs().get(0);

        assertThat(record.getElements().get(0), Matchers.instanceOf(ClassRecord.class));
        assertThat(record.getElements().get(1), Matchers.instanceOf(ClassRecord.class));
        assertThat(record.getElements().get(2), Matchers.instanceOf(ClassRecord.class));
    }

    public static class TakesEmptyObjectArray {

        public static void main(String[] args) {
            new TakesEmptyObjectArray().accept(new Object[]{});
        }

        public void accept(Object[] array) {
        }
    }

    public static class TakesStringArrayWithSomeString {

        public static void main(String[] args) {
            new TakesStringArrayWithSomeString().accept(new String[]{
                    "A",
                    "B",
                    "C",
                    "D",
                    "E",
                    "F"
            });
        }

        public void accept(String[] array) {
        }
    }

    public static class TakesVariousItemsArray {

        public static void main(String[] args) {
            new TakesVariousItemsArray().accept(new Object[]{
                    new X(),
                    664,
                    Object.class,
                    "asdd",
                    new X()
            });
        }

        public void accept(Object[] array) {
            System.out.println(array);
        }
    }

    private static class X {
        public X() {
        }
    }

    public static class VaragsTestCase {

        public static void main(String[] args) {
            takeVararg(new Object[]{Byte[].class, String.class, Integer.class, int[].class, int.class});
        }

        private static void takeVararg(Object[] commonClasses) {
            for (Object b : commonClasses) {
                System.out.println(b);
            }
        }
    }
}
