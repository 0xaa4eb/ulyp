package com.agent.tests.general;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import org.junit.Test;

public class RecordAllTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordAllMethods() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(MultithreadedExample.class)
                        .withMethodToRecord(MethodMatcher.parse("*.*"))
        );

        // threads have two recording sessions each (constructor + method call)
        recordingResult.assertRecordingSessionCount(3);
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

    public static class Clazz implements Interface {

    }

    public static class TestCases {

        public static void main(String[] args) {
            System.out.println(new Clazz().foo());
            System.out.println(new Clazz().bar());
            System.out.println(new Clazz().zoo());
        }
    }

    public static class MultithreadedExample2 {

        public static void main(String[] args) throws InterruptedException {
            Thread t1 = new Thread(() -> {
                new Clazz().bar();
                new Clazz().bar();
                new Clazz().bar();
            });
            t1.start();
            t1.join();

            System.out.println(new Clazz().foo());
        }
    }

    static class X {

        private static int a() {
            return 1;
        }

        private static int b() {
            return 2;
        }

        public static void main(String[] args) throws InterruptedException {
            Thread thread = new Thread(
                    () -> {
                        System.out.println(a());
                        System.out.println(b());
                    }
            );
            thread.start();
            thread.join();
        }
    }
}
