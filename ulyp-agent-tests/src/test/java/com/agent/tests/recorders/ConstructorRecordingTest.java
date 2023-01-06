package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.NotRecordedObjectRecord;
import com.ulyp.storage.CallRecord;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class ConstructorRecordingTest extends AbstractInstrumentationTest {

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
    public void testHappyPathConstructor() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withRecordConstructors()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("main")
        );

        assertThat(root.getChildren(), Matchers.hasSize(1));

        CallRecord constructorCallRecord = root.getChildren().get(0);

        assertThat(constructorCallRecord.getMethod().getName(), is("<init>"));
        assertThat(constructorCallRecord.getMethod().getDeclaringType().getName(), is("com.agent.tests.recorders.ConstructorRecordingTest$X"));
    }

    @Test
    public void testConstructorThrown() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withRecordConstructors()
                        .withMainClassName(TestCasesThrows.class)
                        .withMethodToRecord("main")
        );

        assertThat(root.getChildren(), Matchers.hasSize(1));

        CallRecord constructorCallRecord = root.getChildren().get(0);

        assertThat(constructorCallRecord.getMethod().getName(), is("<init>"));
        assertThat(constructorCallRecord.getMethod().getDeclaringType().getName(), is("com.agent.tests.recorders.ConstructorRecordingTest$XThrows"));
        assertFalse(constructorCallRecord.isFullyRecorded());
        assertThat(constructorCallRecord.getReturnValue(), is(NotRecordedObjectRecord.getInstance()));
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
