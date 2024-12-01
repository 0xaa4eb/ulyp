package com.agent.tests.recorders.java;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.NotRecordedObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConstructorRecordingTest extends AbstractInstrumentationTest {

    public static class Base {

    }

    public static class X extends Base {

        public X() {
        }
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
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("main")
        );

        assertThat(root.getChildren(), Matchers.hasSize(1));

        CallRecord record = root.getChildren().get(0);

        assertThat(record.getMethod().getName(), is("<init>"));
        assertThat(record.getMethod().getType().getName(), is("com.agent.tests.recorders.java.ConstructorRecordingTest$X"));

        ObjectRecord callee = record.getCallee();
        assertThat(callee, instanceOf(IdentityObjectRecord.class));
        IdentityObjectRecord identityCallee = (IdentityObjectRecord) callee;
        assertThat(identityCallee.getType().getName(), is("com.agent.tests.recorders.java.ConstructorRecordingTest$X"));
    }

    @Test
    void testConstructorThrown() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withRecordConstructors()
                        .withMainClassName(TestCasesThrows.class)
                        .withMethodToRecord("main")
        );

        assertThat(root.getChildren(), Matchers.hasSize(1));

        CallRecord record = root.getChildren().get(0);

        assertThat(record.getMethod().getName(), is("<init>"));
        assertThat(record.getMethod().getType().getName(), is("com.agent.tests.recorders.java.ConstructorRecordingTest$XThrows"));
        assertFalse(record.isFullyRecorded());
        assertThat(record.getReturnValue(), is(NotRecordedObjectRecord.getInstance()));
        assertThat(record.getCallee(), is(NotRecordedObjectRecord.getInstance()));
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

    public static class TBase {

        public TBase() {
            System.out.println(foo());
            throw new RuntimeException("a");
        }

        public int foo() {
            return 5;
        }
    }

    public static class T extends TBase {

        public T() {

        }
    }

    public static class TestCasesThrows2 {

        public static void bar() {
            try {
                System.out.println(new T());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void main(String[] args) {
            bar();
        }
    }
}
