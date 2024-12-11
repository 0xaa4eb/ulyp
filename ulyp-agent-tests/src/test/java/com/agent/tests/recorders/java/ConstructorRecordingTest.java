package com.agent.tests.recorders.java;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.NotRecordedObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static com.agent.tests.util.RecordingMatchers.isIdentity;
import static com.agent.tests.util.RecordingMatchers.isPrinted;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConstructorRecordingTest extends AbstractInstrumentationTest {

    public static class Base {
    }

    public static class X extends Base {
    }

    public static class TestCase {

        public static void main(String[] args) {
            System.out.println(new X());
        }
    }

    @Test
    void testHappyPathConstructor() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withRecordConstructors()
                        .withMain(TestCase.class)
                        .withMethodToRecord("main")
        );

        assertThat(root.getChildren(), Matchers.hasSize(1));

        CallRecord objectRecord = root.getChildren().get(0);

        assertThat(objectRecord.getMethod().getName(), is("<init>"));
        assertThat(objectRecord.getMethod().getType().getName(), is("com.agent.tests.recorders.java.ConstructorRecordingTest$X"));

        ObjectRecord callee = objectRecord.getCallee();
        assertThat(callee, instanceOf(IdentityObjectRecord.class));
        IdentityObjectRecord identityCallee = (IdentityObjectRecord) callee;
        assertThat(identityCallee.getType().getName(), is("com.agent.tests.recorders.java.ConstructorRecordingTest$X"));
    }

    public static class A {

        private final int data;

        public A(int data) {
            this.data = data;

            if (data > 0) {
                A other = get();
                System.out.println(other);
            }
        }

        public static A get() {
            return new A(0);
        }

        @Override
        public String toString() {
            return String.valueOf(data);
        }
    }

    public static class TestCase2 {

        public static A get() {
            return new A(777);
        }

        public static void main(String[] args) {
            System.out.println(get());
        }
    }

    @Test
    public void testConstructorBlocksRecordersForType() {
        /*
         * If constructor is being called for certain type, this particular type temporarily should not be available
         * for recorders other than identity recorder
         */
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withRecordConstructors()
                        .withMain(TestCase2.class)
                        .withMethodToRecord("get")
                        .withPrintTypes("**.A")
        );

        ObjectRecord printed = root.getReturnValue(); // the return of TestCase2.get()

        assertThat(printed, isPrinted("777"));

        CallRecord constructorCall = root.getChildren().get(0);
        CallRecord getCall = constructorCall.getChildren().get(0);

        assertThat(getCall.getReturnValue(), isIdentity());
    }

    @Test
    void testConstructorThrown() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withRecordConstructors()
                        .withMain(TestCasesThrows.class)
                        .withMethodToRecord("main")
        );

        assertThat(root.getChildren(), Matchers.hasSize(1));

        CallRecord objectRecord = root.getChildren().get(0);

        assertThat(objectRecord.getMethod().getName(), is("<init>"));
        assertThat(objectRecord.getMethod().getType().getName(), is("com.agent.tests.recorders.java.ConstructorRecordingTest$XThrows"));
        assertFalse(objectRecord.isFullyRecorded());
        assertThat(objectRecord.getReturnValue(), is(NotRecordedObjectRecord.getInstance()));
        assertThat(objectRecord.getCallee(), is(NotRecordedObjectRecord.getInstance()));
    }

    public static class XThrows extends Base {

        public XThrows() {
            throw new RuntimeException("a");
        }
    }

    public static class TestCasesThrows {

        public static void main(String[] args) {
            try {
                System.out.println(new XThrows());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
