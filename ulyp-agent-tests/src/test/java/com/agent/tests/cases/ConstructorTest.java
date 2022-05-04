package com.agent.tests.cases;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.storage.CallRecord;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertThat;

@Ignore
public class ConstructorTest extends AbstractInstrumentationTest {

    // -----------------------------------------------------------------------------------------------------------------

    public static class Base {

    }

    public static class X extends Base {

        public X() {
            System.out.println(43);
        }
    }

    public static class TestCases {

        public static void main(String[] args) {
            System.out.println(new X());
        }
    }

    @Test
    public void testHappyPathConstructor() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(TestCases.class)
                        .setMethodToRecord("main")
        );

        assertThat(root.getChildren(), Matchers.hasSize(1));

        CallRecord xConstructorCall = root.getChildren().get(0);

        assertThat(xConstructorCall.getMethod().getName(), Matchers.is("<init>"));
        assertThat(xConstructorCall.getClassName(), Matchers.is("com.agent.tests.cases.ConstructorTest$X"));

        assertThat(xConstructorCall.getChildren(), Matchers.hasSize(1));
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static class X3 extends Base {

        public X3() {
            throw new RuntimeException("a");
        }
    }

    public static class TestCasesThrows {

        public static void main(String[] args) {
            try {
                System.out.println(new X3());
            } catch (Exception e) {

            }
        }
    }

    @Test
    public void testConstructorThrown() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(TestCasesThrows.class)
                        .setMethodToRecord("main")
        );

        assertThat(root.getChildren(), Matchers.hasSize(1));

        CallRecord ctr = root.getChildren().get(0);

        assertThat(ctr.getMethod().getName(), Matchers.is("<init>"));
        assertThat(ctr.getClassName(), Matchers.is("com.agent.tests.cases.ConstructorTest$X3"));
    }

    // -----------------------------------------------------------------------------------------------------------------

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

            }
        }

        public static void main(String[] args) {
            bar();
        }
    }

    @Test
    public void testConstructorThrownInsideMethodCalls() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(TestCasesThrows2.class)
                        .setMethodToRecord("main")
        );

        assertThat(root.getChildren(), Matchers.hasSize(1));

        CallRecord ctr = root.getChildren().get(0);

        assertThat(ctr.getMethod().getName(), Matchers.is("<init>"));
        assertThat(ctr.getClassName(), Matchers.is("com.agent.tests.cases.ConstructorTest$X3"));
    }
}
