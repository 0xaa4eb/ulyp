package com.agent.tests.recorders.java.arrays;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.*;
import com.ulyp.core.recorders.arrays.ArrayRecord;
import com.ulyp.core.recorders.basic.ClassRecord;
import com.ulyp.core.recorders.basic.StringObjectRecord;
import com.ulyp.core.recorders.numeric.NumberRecord;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        List<? extends ObjectRecord> items = record.getElements();

        assertThat(items, Matchers.hasSize(3));
        com.ulyp.core.recorders.basic.StringObjectRecord str0 = (com.ulyp.core.recorders.basic.StringObjectRecord) items.get(0);
        assertEquals(str0.value(), "A");
        com.ulyp.core.recorders.basic.StringObjectRecord str1 = (com.ulyp.core.recorders.basic.StringObjectRecord) items.get(1);
        assertEquals(str1.value(), "B");
        com.ulyp.core.recorders.basic.StringObjectRecord str2 = (com.ulyp.core.recorders.basic.StringObjectRecord) items.get(2);
        assertEquals(str2.value(), "C");
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

        List<? extends ObjectRecord> items = record.getElements();

        assertThat(items, Matchers.hasSize(5));
        com.ulyp.core.recorders.basic.StringObjectRecord str0 = (com.ulyp.core.recorders.basic.StringObjectRecord) items.get(0);
        assertEquals(str0.value(), "A");
        com.ulyp.core.recorders.basic.StringObjectRecord str1 = (com.ulyp.core.recorders.basic.StringObjectRecord) items.get(1);
        assertEquals(str1.value(), "B");
        com.ulyp.core.recorders.basic.StringObjectRecord str2 = (com.ulyp.core.recorders.basic.StringObjectRecord) items.get(2);
        assertEquals(str2.value(), "C");
        com.ulyp.core.recorders.basic.StringObjectRecord str3 = (com.ulyp.core.recorders.basic.StringObjectRecord) items.get(3);
        assertEquals(str3.value(), "D");
        com.ulyp.core.recorders.basic.StringObjectRecord str4 = (StringObjectRecord) items.get(4);
        assertEquals(str4.value(), "E");
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

        List<? extends ObjectRecord> items = record.getElements();

        IdentityObjectRecord arg0 = (IdentityObjectRecord) items.get(0);
        assertThat(arg0.getType().getName(), is(X.class.getName()));

        com.ulyp.core.recorders.numeric.NumberRecord arg1 = (NumberRecord) items.get(1);
        assertThat(arg1.getNumberPrintedText(), is("664"));

        com.ulyp.core.recorders.basic.ClassRecord arg4 = (com.ulyp.core.recorders.basic.ClassRecord) items.get(2);
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

        assertThat(record.getElements().get(0), Matchers.instanceOf(com.ulyp.core.recorders.basic.ClassRecord.class));
        assertThat(record.getElements().get(1), Matchers.instanceOf(com.ulyp.core.recorders.basic.ClassRecord.class));
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
