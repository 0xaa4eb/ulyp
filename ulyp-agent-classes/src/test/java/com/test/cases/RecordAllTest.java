package com.test.cases;

import com.test.cases.util.ForkProcessBuilder;
import com.test.cases.util.RecordingResult;
import com.ulyp.core.CallRecordDatabase;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.Recording;
import com.ulyp.storage.StorageException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class RecordAllTest extends AbstractInstrumentationTest {

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

/*
//    TODO
    @Test
    public void test() {

        RecordingResult recordingResult = runForkProcess(
                new ForkProcessBuilder()
                        .setMainClassName(MultithreadedExample2.class)
                        .setMethodToRecord(MethodMatcher.parse("*.*"))
        );

        Map<Integer, Recording> results = recordingResult.aggregateByThread();

        Recording recorded = results.values()
                .stream()
                .filter(database -> database.getRoot().getChildren().size() == 3)
                .findAny()
                .orElseThrow(() -> new AssertionError("Could not find recording result for"));
    }

    @Test
    public void shouldAggregateByThread() throws StorageException {

        RecordingResult recordingResult = runForkProcess(
                new ForkProcessBuilder()
                        .setMainClassName(X.class)
                        .setMethodToRecord("*")
        );

        Map<Long, CallRecordDatabase> longCallRecordDatabaseMap = recordingResult.aggregateByThread();

        Assert.assertEquals(2, longCallRecordDatabaseMap.size());

        CallRecordDatabase callRecordDatabase = longCallRecordDatabaseMap.values().stream().filter(db -> db.countAll() == 3L).findFirst().orElseThrow(
                () -> new AssertionError("Could not find db with 3 call recods")
        );

        CallRecord root = callRecordDatabase.getRoot();

        Assert.assertThat(root.getMethodName(), is("run"));
        Assert.assertThat(root.getClassName(), is("java.lang.Thread"));
        Assert.assertThat(root.getChildren(), hasSize(2));
    }
 */

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
