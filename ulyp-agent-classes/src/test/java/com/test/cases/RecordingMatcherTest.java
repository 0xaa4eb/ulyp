package com.test.cases;

import com.test.cases.util.RecordingResult;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.CallRecord;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RecordingMatcherTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordMainMethodIfMatcherIsNotSpecified() {
        CallRecord root = runForkWithUi(new ForkProcessBuilder().setMainClassName(TestCases.class));

        assertThat(root.getMethodName(), is("main"));
        assertThat(root.getClassName(), is(TestCases.class.getName()));
        assertThat(root.getChildren(), Matchers.hasSize(3));
    }

    @Test
    public void testRecordViaInterfaceMatcher() {
        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCases.class)
                        .setMethodToRecord(MethodMatcher.parse("Interface.foo"))
        );

        Assert.assertNotNull(root);
    }

    @Test
    public void shouldRecordAllMethods() {
        RecordingResult recordingResult = runForkProcess(
                new ForkProcessBuilder()
                        .setMainClassName(MultithreadedExample.class)
                        .setMethodToRecord(MethodMatcher.parse("*.*"))
        );

        // threads have two recording sessions each (constructor + method call)
        recordingResult.assertRecordingSessionCount(3);
    }

    public static class MultithreadedExample {

        public static void main(String[] args) throws InterruptedException {
            Thread t1 = new Thread(() -> new Clazz().bar());
            t1.start();

            Thread t2 = new Thread(() -> new Clazz().zoo());
            t2.start();

            t1.join();
            t2.join();

            System.out.println(new Clazz().foo());
        }
    }

    public interface Interface {

        default int foo() {
            return 42;
        }

        default int bar() {
            return 1;
        }

        default int zoo() {
            return 2;
        }
    }

    public static class Clazz implements Interface {

    }

    public static class TestCases {

        public static void main(String[] args) {
            System.out.println(new Clazz().foo());
            System.out.println(new Clazz().bar());
            System.out.println(new Clazz().zoo());
        }
    }
}
