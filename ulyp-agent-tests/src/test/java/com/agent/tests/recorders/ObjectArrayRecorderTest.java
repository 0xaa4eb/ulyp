package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.*;
import com.ulyp.core.recorders.arrays.ObjectArrayRecord;
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
                        .withMainClassName(TakesEmptyObjectArray.class)
                        .withMethodToRecord("accept")
        );


        IdentityObjectRecord record = (IdentityObjectRecord) root.getArgs().get(0);


        assertThat(record.getType().getName(), is("[Ljava.lang.Object;"));
    }

    @Test
    void shouldProvideArgumentTypes() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TakesEmptyObjectArray.class)
                        .withMethodToRecord("accept")
                        .withRecordArrays()
        );


        ObjectArrayRecord record = (ObjectArrayRecord) root.getArgs().get(0);


        assertThat(record.getLength(), is(0));
        assertThat(record.getRecordedItems(), Matchers.empty());
    }

    @Test
    void shouldRecordSimpleArrayWithString() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TakesStringArrayWithSomeString.class)
                        .withMethodToRecord("accept")
                        .withRecordArrays()
        );


        ObjectArrayRecord record = (ObjectArrayRecord) root.getArgs().get(0);


        assertThat(record.getLength(), is(6));

        List<ObjectRecord> items = record.getRecordedItems();

        assertThat(items, Matchers.hasSize(3));
        StringObjectRecord str0 = (StringObjectRecord) items.get(0);
        assertEquals(str0.value(), "A");
        StringObjectRecord str1 = (StringObjectRecord) items.get(1);
        assertEquals(str1.value(), "B");
        StringObjectRecord str2 = (StringObjectRecord) items.get(2);
        assertEquals(str2.value(), "C");
    }

    @Test
    void shouldRecordSimpleArrayWithStringWithCustomMaxCountSpecified() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TakesStringArrayWithSomeString.class)
                        .withMethodToRecord("accept")
                        .withRecordArrays()
                        .withRecordArrayItems(5)
        );


        ObjectArrayRecord record = (ObjectArrayRecord) root.getArgs().get(0);


        assertThat(record.getLength(), is(6));

        List<ObjectRecord> items = record.getRecordedItems();

        assertThat(items, Matchers.hasSize(5));
        StringObjectRecord str0 = (StringObjectRecord) items.get(0);
        assertEquals(str0.value(), "A");
        StringObjectRecord str1 = (StringObjectRecord) items.get(1);
        assertEquals(str1.value(), "B");
        StringObjectRecord str2 = (StringObjectRecord) items.get(2);
        assertEquals(str2.value(), "C");
        StringObjectRecord str3 = (StringObjectRecord) items.get(3);
        assertEquals(str3.value(), "D");
        StringObjectRecord str4 = (StringObjectRecord) items.get(4);
        assertEquals(str4.value(), "E");
    }

    @Test
    void testUserDefinedClassArrayWith3Elements() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TakesVariousItemsArray.class)
                        .withMethodToRecord("accept")
                        .withRecordArrays()
        );


        ObjectArrayRecord record = (ObjectArrayRecord) root.getArgs().get(0);

        assertThat(record.getLength(), is(5));

        List<ObjectRecord> items = record.getRecordedItems();

        IdentityObjectRecord arg0 = (IdentityObjectRecord) items.get(0);
        assertThat(arg0.getType().getName(), is(X.class.getName()));

        NumberRecord arg1 = (NumberRecord) items.get(1);
        assertThat(arg1.getNumberPrintedText(), is("664"));

        ClassRecord arg4 = (ClassRecord) items.get(2);
        assertThat(arg4.getDeclaringType().getName(), is(Object.class.getName()));
    }

    @Test
    void testVarargs() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(VaragsTestCase.class)
                        .withMethodToRecord("takeVararg")
                        .withRecordArrays()
        );


        ObjectArrayRecord record = (ObjectArrayRecord) root.getArgs().get(0);

        assertThat(record.getRecordedItems().get(0), Matchers.instanceOf(ClassRecord.class));
        assertThat(record.getRecordedItems().get(1), Matchers.instanceOf(ClassRecord.class));
        assertThat(record.getRecordedItems().get(2), Matchers.instanceOf(ClassRecord.class));
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
